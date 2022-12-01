import numpy as np
import cv2
from filter import *

def input_int(default_val, input_prompt):
  user_input = input(input_prompt)
  try:
    int_val = int(user_input)
    return True, int_val
  except:
    return False, default_val

def input_float(default_val, input_prompt):
  user_input = input(input_prompt)
  try:
    float_val = float(user_input)
    return True, float_val
  except:
    return False, default_val

# --------------------------------------------------------------------------------------------------------------------------------- #

# structure of rects
# displacement starts from bottom left corner 

# [0] = start x
# [1] = start y
# [2] = rect width
# [3] = rect height

# Find the overlapping region in percentage between 2 rects
# Divides the area of the overlapping region multiplied by 2 over the summed area of the original rects
def find_overlap_percentage_in_rects(rect1, rect2):
  overlap_start_x = rect1[0] if rect1[0] > rect2[0] else rect2[0]
  overlap_end_x = rect1[0] + rect1[2] if (rect1[0] + rect1[2]) < (rect2[0] + rect2[2]) else rect2[0] + rect2[2]

  overlap_start_y = rect1[1] if rect1[1] > rect2[1] else rect2[1]
  overlap_end_y = rect1[1] + rect1[3] if (rect1[1] + rect1[3]) < (rect2[1] + rect2[3]) else rect2[1] + rect2[3]

  overlap_width = overlap_end_x - overlap_start_x
  overlap_height = overlap_end_y - overlap_start_y
  overlap_area = overlap_width * overlap_height

  rect1_area = rect1[2] * rect1[3]
  rect2_area = rect2[2] * rect2[3]

  # Doubling the overlapped area because it is part of both rects
  return (overlap_area * 2) / (rect1_area + rect2_area)

# Constrains the given rect into the container
# Returns a boolean to determime if the rect will fit into the container at all (Ignore the rect if it returns false)
def constrain_rect_into_container(rect, container):
  cannon_contain_rect = False
  rect_end_x = rect[0] + rect[2]
  rect_end_y = rect[1] + rect[3]

  container_end_x = container[0] + container[2]
  container_end_y = container[1] + container[3]

  start_x = rect[0]
  start_y = rect[1]
  width = rect[2]
  height = rect[3]

  if rect[0] < container[0]:
    start_x = container[0]
  
  if rect[0] > container_end_x:
    cannot_contain_rect = True

  if rect[1] < container[1]:
    start_y = container[1]

  if rect[1] > container_end_y:
    cannot_contain_rect = True

  if rect_end_x > container_end_x:
    width = rect[2] - (rect_end_x - container_end_x)

  if rect_end_x < container[0]:
    cannot_contain_rect = True

  if rect_end_y > container_end_y:
    height = rect[3] - (rect_end_y - container_end_y)

  if rect_end_y < container[1]:
    cannot_contain_rect = True

  return cannon_contain_rect, (start_x, start_y, width, height)

def cast_rect_as_integer(rect):
  return (int(rect[0]), int(rect[1]), int(rect[2]), int(rect[3]))

# --------------------------------------------------------------------------------------------------------------------------------- #

# Blurring the image
# Global variables used : blur_mask_type, blur_mask_cutoff_radius, blur_mask_btw_order
def apply_point_multiplication_to_freq_domain(image, point_multiplication_map):
  (image_height, image_width, image_channels) = image.shape

  blurred_image = np.zeros(image.shape)
  for i in range(0, 3):
    freq_domain = np.fft.fftshift(np.fft.fft2(image[:, :, i]))
    blurred_freq_domain = point_multiplication_map * freq_domain
    spatial_domain = np.real(np.fft.ifft2(np.fft.ifftshift(blurred_freq_domain)))

    for x in range(0, image_width):
      for y in range(0, image_height):
        pixel_val = spatial_domain[y, x]
        if pixel_val < 0:
          pixel_val = 0
        if pixel_val > 255:
          pixel_val = 255
        
        blurred_image[y, x, i] = pixel_val
  return blurred_image

# --------------------------------------------------------------------------------------------------------------------------------- #

# Identify and blur faces in the video
# Global variables used : detectMultiScale_scaleFactor, detectMultiScale_minNeighbours
class VideoFaceBlurring:
  # ref -> https://pyimagesearch.com/2018/07/30/opencv-object-tracking/
  # Found the tracker in cv2 library that allows you to track a section in the image
  # a list of trackers cv2 offers
  cv2_trackers = {"csrt": cv2.TrackerCSRT_create,
                  "kcf": cv2.TrackerKCF_create,
                  "boosting": cv2.legacy.TrackerBoosting_create,
                  "mil": cv2.TrackerMIL_create,
                  "tld": cv2.legacy.TrackerTLD_create,
                  "medianflow": cv2.legacy.TrackerMedianFlow_create,
                  "mosse": cv2.legacy.TrackerMOSSE_create}

  def __init__(self, cascade_xml_path = "face_detector.xml", detectMultiScale_scaleFactor = 1.3, detectMultiScale_minNeighbours = 5, face_overlap_percentage_threshold_min = 0.4, tracker_type = "MOSSE", blur_mask_type = "btw", blur_mask_cutoff_radius = 3, blur_mask_btw_order = 5):
    # The face detection model
    self.face_cascade = cv2.CascadeClassifier(cascade_xml_path)

    self.detectMultiScale_scaleFactor = detectMultiScale_scaleFactor
    self.detectMultiScale_minNeighbours = detectMultiScale_minNeighbours

    # Defining a list to store the trackers
    self.trackers = []
    self.tracker_type = tracker_type
    self.face_overlap_percentage_threshold_min = face_overlap_percentage_threshold_min

    self.lp_filter_args = [0, 0, blur_mask_type, blur_mask_cutoff_radius, blur_mask_btw_order]
    
  def apply(self, frame):
    frame_rect = (0, 0, frame.shape[1], frame.shape[0])
    faces = self.face_cascade.detectMultiScale(frame, self.detectMultiScale_scaleFactor, self.detectMultiScale_minNeighbours) 
    rects_to_blur = self.detect_and_track_faces(frame, faces, self.trackers)
    
    for (start_x, start_y, rect_width, rect_height) in rects_to_blur:
      # Creating the mask
      self.lp_filter_args[0] = rect_width
      self.lp_filter_args[1] = rect_height
      blur_mask = lpfilter(*self.lp_filter_args)

      # Some testing found some rects that are out of bounds.
      # Therefore, we catch it then only apply a function to shift the rect back into the boundaries of the frame.
      # However, it is computationally expensive to just apply it to the source, so we catch it here.
      try:
        frame[start_y:start_y+rect_height, start_x:start_x+rect_width] = apply_point_multiplication_to_freq_domain(frame[start_y:start_y+rect_height, start_x:start_x+rect_width], blur_mask)
      except:
        rect_in_image, (start_x, start_y, rect_width, rect_height) = constrain_rect_into_container((start_x, start_y, rect_width, rect_height), frame_rect)
        if rect_in_image:
          frame[start_y:start_y+rect_height, start_x:start_x+rect_width] = apply_point_multiplication_to_freq_domain(frame[start_y:start_y+rect_height, start_x:start_x+rect_width], blur_mask)
        
    return frame

  # The given HAAR model isn't enough to detect the faces at every frame
  # When the person turns their head, the model cannot detect the face
  # Therefore we used CV2 trackers to help improve the accuracy of identifying the face

  # ref -> https://pyimagesearch.com/2018/07/30/opencv-object-tracking/
  def detect_and_track_faces(self, image, faces, trackers):
    final_rects = []
  
    # update the existing trackers
    for tracker in trackers:
      # The tracker will try to find the face again and return if the operation is successful, if it is then return the tracked bounding box
      track_success, tracked_bbox = tracker.update(image)
      # If the face is still detected
      if track_success:
        final_rects.append(cast_rect_as_integer(tracked_bbox))
        
        # Find the nearest overlapping face, and remove the face.
        # This leaves a set of faces that have no tracker (we add new trackers)
        for i in range(len(faces) - 1, -1, -1):
          face = faces[i]
          # find the area that both rects overlap
          overlap = find_overlap_percentage_in_rects(face, tracked_bbox)
          if overlap > self.face_overlap_percentage_threshold_min:
            # found an overlapping face, so remove it
            faces = np.delete(faces, i, axis=0)
            break

      else:
        # Else, the face has likely moved out of the frame
        # So we can remove the tracker
        trackers.remove(tracker)
    
    # add new trackers
    # (start_x, start_y, rect_width, rect_height)
    for face in faces: 
      # Create the tracker
      tracker = VideoFaceBlurring.cv2_trackers[self.tracker_type.lower()]()

      # Initialise it to track the face
      tracker.init(image, face)
      
      # Keep track of it
      trackers.append(tracker)

      final_rects.append(face)
      
    return final_rects

# --------------------------------------------------------------------------------------------------------------------------------- #

# Overlaying a video on top of the target video
class VideoOverlaying:
  def __init__(self, overlay_vid_path = "talking.mp4", overlay_vid_scale_percent = 25, overlay_vid_border_radius = 5, overlay_vid_position_top = 50, overlay_vid_position_left = 50):
    # overlay video initialization
    self.overlay_vid = cv2.VideoCapture(overlay_vid_path)
    self.overlay_vid_frame_count = int(self.overlay_vid.get(cv2.CAP_PROP_FRAME_COUNT))

    self.vid_scaled_width = int(self.overlay_vid.get(cv2.CAP_PROP_FRAME_WIDTH) * overlay_vid_scale_percent / 100)
    self.vid_scaled_height = int(self.overlay_vid.get(cv2.CAP_PROP_FRAME_HEIGHT) * overlay_vid_scale_percent / 100)
    self.vid_scaled_dim = (self.vid_scaled_width, self.vid_scaled_height) 

    # Region and position of the overlaid area
    # Rects are defined as tuples with the structure:
    # [0] - start x
    # [1] - end x
    # [2] - start y
    # [3] - end y
    self.overlay_rect = (overlay_vid_position_left,
                         overlay_vid_position_left + self.vid_scaled_width,
                         overlay_vid_position_top, 
                         overlay_vid_position_top + self.vid_scaled_height)
    self.overlay_rect_with_border = (self.overlay_rect[0] - overlay_vid_border_radius,
                                     self.overlay_rect[1] + overlay_vid_border_radius,
                                     self.overlay_rect[2] - overlay_vid_border_radius,
                                     self.overlay_rect[3] + overlay_vid_border_radius)

    self.overlay_vid_frame_counter = 0

    print("Scaled overlay video resolution : ", str(self.vid_scaled_width), " x ", str(self.vid_scaled_height), sep="")

  def apply(self, frame):
    # Reading the frames
    overlay_read_success, overlay_frame = self.overlay_vid.read()

    # Counter to remember when to restart the overlay video
    self.overlay_vid_frame_counter += 1

    # Loop the overlay video
    if self.overlay_vid_frame_counter == self.overlay_vid_frame_count:
      self.overlay_vid_frame_counter = 0
      self.overlay_vid.set(cv2.CAP_PROP_POS_FRAMES, 0)
    
    # Checking just in case
    if overlay_read_success:
      # Resize the overlaid frame
      overlay_frame_resized = cv2.resize(overlay_frame, self.vid_scaled_dim, interpolation=cv2.INTER_AREA)
      # Replace the pixels of the overlaid region
      frame[self.overlay_rect_with_border[2]:self.overlay_rect_with_border[3], self.overlay_rect_with_border[0]:self.overlay_rect_with_border[1]] = 0
      frame[self.overlay_rect[2]:self.overlay_rect[3], self.overlay_rect[0]:self.overlay_rect[1]] = overlay_frame_resized
      
    return frame

# --------------------------------------------------------------------------------------------------------------------------------- #

# Overlaying alternating watermark images on the target video
# Global variables used : watermark_switch_interval_seconds
class VideoWatermarks:
  # default to a pink colour
  def __init__(self, vid_width, vid_height, frame_rate, watermark1_path = "watermark1.png", watermark2_path = "watermark2.png", watermark_switch_interval_seconds = 5, watermark_colour = [255, 192, 203]):
    vid_dim = (vid_width, vid_height)
    
    # watermark images initialisation
    watermark_gray_images = []
    for watermark_path in (watermark1_path, watermark2_path):
      watermark = cv2.imread(watermark_path, 1)
      watermark_gray = cv2.cvtColor(watermark, cv2.COLOR_BGR2GRAY)

      if watermark_gray.shape != vid_dim:
        watermark_gray = cv2.resize(watermark_gray, vid_dim, interpolation=cv2.INTER_AREA)
      
      watermark_gray_images.append(watermark_gray)

    self.watermark_switch_interval_seconds = watermark_switch_interval_seconds

    #some placeholders and variables
    threshold = 10
    [self.r, self.g, self.b] = watermark_colour 
    self.interval_counter = 0
    self.watermark_switch_bit = 0

    self.coords_list_1 = []
    self.coords_list_2 = []
    #storing needed pixels by their coords for both watermarks
    for y in range(0, vid_height):
      for x in range(0, vid_width):
        if watermark_gray_images[0][y, x] > threshold:
          self.coords_list_1.append([y, x])
        if watermark_gray_images[1][y, x] > threshold:
          self.coords_list_2.append([y, x])

    print("Non-empty pixels in watermark 1 : ", len(self.coords_list_1), sep="")
    # print("Coords of mentioned pixels in watermark 1 : ", self.coords_list_1, sep="")

    print("Non-empty pixels in watermark 2 : ", len(self.coords_list_2), sep="")
    # print("Coords of mentioned pixels in watermark 2 : ", self.coords_list_2, sep="")
  
  def apply(self, frame):
    # getting the correct watermark image to use
    coords_list = self.coords_list_1 if self.watermark_switch_bit == 0 else self.coords_list_2

    for coords in coords_list:
      # custom colours for the watermark
      frame[coords[0], coords[1], 0] = self.b
      frame[coords[0], coords[1], 1] = self.g
      frame[coords[0], coords[1], 2] = self.r
      
    # interval counter to switch between watermarks
    self.interval_counter += 1
    if self.interval_counter % (self.watermark_switch_interval_seconds * frame_rate) == 0:
        self.watermark_switch_bit = (self.watermark_switch_bit + 1) % 2
    return frame

# --------------------------------------------------------------------------------------------------------------------------------- #

def process_video(target_vid, output_vid, *modifiers):
  frame_counter = 0
  while target_vid.isOpened(): 
    read_success, frame = target_vid.read()
    if read_success:
      print("At frame " + str(frame_counter))
      for modifier in modifiers:
        frame = modifier(frame)
      output_vid.write(frame)
      frame_counter += 1
    else:
      break

# --------------------------------------------------------------------------------------------------------------------------------- #

# default values for the parameters of the program

# parameters to adjust the file paths
target_vid_path = "street.mp4"
output_vid_path = "processed_video.mp4"

# inputs
video_face_blurring_args = None
video_overlay_args = None
video_watermarks_args = None

if input("Enter 'Y' to use the demo version, and 'N' to enter the input and output file paths\n").lower() == "n":
  print("\nI/O settings")
  target_vid_path = input("Please enter the file path to the video you want to process\n")
  output_vid_path = input("Please enter the output path for the processed video\n")

  if input("Enter 'Y' to skip advanced configurations, and 'N' to configure it\n").lower() == "n":
      
    if input("Enter 'Y' to skip face blurring settings, and 'N' to configure it\n").lower() == "n":
      video_face_blurring_args = []

      print("\nFace detection settings")
      video_face_blurring_args.append(input("Please enter the file path to pretrained HAAR model\n"))
      video_face_blurring_args.append(input_float(1.3, "Please enter scale factor for the HAAR cascade model\n")[1])
      video_face_blurring_args.append(input_int(5, "Please enter min neighbours for the HAAR cascade model\n")[1]) 
      video_face_blurring_args.append(input_float(0.4, "Please enter the face tracking overlap percentage threshold (0.0 to 1.0) (Used when trying to find which face detected by the HAAR cascade model belongs to which tracker)\n")[1])
      video_face_blurring_args.append(input("Please enter the type of cv2 tracker to use\n"))

      print("\nBlur settings")
      video_face_blurring_args.append(input("Please enter the type of mask for blurring the image ('ideal', 'btw', 'gaussian'\n"))
      video_face_blurring_args.append(input_int(3, "Please enter the blur mask cutoff radius\n")[1])
      if (video_face_blurring_args[4] == 'btw'):
        video_face_blurring_args.append(input_int(5, "Please enter the blur mask order\n")[1])
      else:
        video_face_blurring_args.append(5)
      
    if input("Enter 'Y' to skip overlay video settings, and 'N' to configure it\n").lower() == "n":
      video_overlay_args = []

      print("\nOverlay video settings")
      video_overlay_args.append(input("Please enter the file path to the video you want to overlay on top of the target video\n"))
      video_overlay_args.append(input_int(25, "Please enter the scaling factor (out of 100) for the overlay video\n")[1])
      video_overlay_args.append(input_int(5, "Please enter the border radius for the overlay video\n")[1])
      video_overlay_args.append(input_int(50, "Please enter the y position (pixels from the top) for the overlay video\n")[1])
      video_overlay_args.append(input_int(50, "Please enter the x position (pixels from the left) for the overlay video\n")[1])

    if input("Enter 'Y' to skip video watermarks settings, and 'N' to configure it\n").lower() == "n":
      video_watermarks_args = []

      print("\nVideo watermarks settings")
      video_watermarks_args.append(input("Please enter the file path to watermark1\n"))
      video_watermarks_args.append(input("Please enter the file path to watermark2\n"))
      video_watermarks_args.append(input_int(5, "Please enter the interval (in seconds) to switch between watermark1 and watermark2\n")[1])

      # default pink colour
      water_mark_colour = []
      for colour_channel, default_val in [("r", 255), ("g", 192), ("b", 203)]:
        _, colour = input_int(default_val, "Please enter the watermark colour - '" + colour_channel + "' channel\n")
        if colour > 255:
          colour = 255
        if colour < 0:
          colour = 0
        water_mark_colour.append(colour)
      video_watermarks_args.append(water_mark_colour)
    
# --------------------------------------------------------------------------------------------------------------------------------- #

# target video initialization
target_vid = cv2.VideoCapture(target_vid_path) 
vid_width = int(target_vid.get(cv2.CAP_PROP_FRAME_WIDTH))
vid_height = int(target_vid.get(cv2.CAP_PROP_FRAME_HEIGHT))
frame_rate = int(target_vid.get(cv2.CAP_PROP_FPS))
frame_count = int(target_vid.get(cv2.CAP_PROP_FRAME_COUNT))

# output video initialization
output_video_format = "mp4v" # "MJPG"
output_fourcc = cv2.VideoWriter_fourcc(*output_video_format)
print(output_vid_path)
output_vid = cv2.VideoWriter(output_vid_path, output_fourcc, frame_rate, (vid_width, vid_height))

print("Input resolution : ", str(vid_width), " x ", str(vid_height), sep="")
print("Frames : ", str(frame_count), ", at ", str(frame_rate), sep="")

video_face_blurring = VideoFaceBlurring() if video_face_blurring_args == None else VideoFaceBlurring(*video_face_blurring_args)
video_overlaying = VideoOverlaying() if video_overlay_args == None else VideoOverlaying(*video_overlay_args)
video_watermarks = VideoWatermarks(vid_width, vid_height, frame_rate) if video_watermarks_args == None else VideoWatermarks(vid_width, vid_height, frame_rate, *video_watermarks_args)

print("\nProcessing Video")
process_video(target_vid, 
              output_vid, 
              video_face_blurring.apply,
              video_overlaying.apply,
              video_watermarks.apply)

output_vid.release()
target_vid.release()