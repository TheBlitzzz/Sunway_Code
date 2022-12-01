import cv2
import numpy as np

# remove margins

def removeMargins(img):
    
    # binarize image
    
    _,imgBinarized = cv2.threshold(img, 240, 255, cv2.THRESH_BINARY)
    imgBinarized[imgBinarized == 0] = 1
    imgBinarized[imgBinarized == 255] = 0
    
    # histogram projection
    
    horiProj = np.sum(imgBinarized,axis=1)
    
    vertProj = np.sum(imgBinarized,axis=0)
    
    # detect first and last non-zero values
    
    [nrow,ncol] = img.shape
    
    for x in range(0,nrow-1):
        if horiProj[x] != 0:
            a = x
            break
    
    for x in range(nrow-1,0,-1):
        if horiProj[x] != 0:
            b = x
            break
        
    for y in range(0,ncol-1):
        if vertProj[y] != 0:
            c = y
            break
        
    for y in range(ncol-1,0,-1):
        if vertProj[y] != 0:
            d = y
            break
    
    # remove margins
    
    img = img[a:b+1,c:d+1]
    
    return img

# remove tables

def removeTables(img):
    
    # binarize image
    
    _,imgBinarized = cv2.threshold(img, 200, 255, cv2.THRESH_BINARY)
    imgBinarized[imgBinarized == 0] = 1
    imgBinarized[imgBinarized == 255] = 0
    
    # histogram projection
    
    horiProj = np.sum(imgBinarized,axis=1)
    
    # we only need the horizontal projection to detect tables
    
    [nrow,ncol] = img.shape
    
    # to detect tables, first, we find rows in the image that are 70% filled.
    
    tableBorder = []
    
    for x in range(0,nrow-1):
        if horiProj[x] >= 0.7*ncol:
            tableBorder.append(x-1) # -1 to account for anti-aliasing.
    
    # second, we find the first zero value row after the table border and destroy all data in those rows.
    
    for a in range(0,len(tableBorder)):
        for x in range(tableBorder[a],nrow-1):
            if horiProj[x] == 0:
                img[tableBorder[a]:x,:] = 255
                break
    
    return img

# split columns

def splitColumns(img):
    
    # binarize image
    
    _,imgBinarized = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY)
    imgBinarized[imgBinarized == 0] = 1
    imgBinarized[imgBinarized == 255] = 0
    
    # vertical dilation
    
    SE = cv2.getStructuringElement(cv2.MORPH_RECT,(13,101))

    dilated = cv2.dilate(imgBinarized,SE,iterations=1)
    
    contours,hierarchy = cv2.findContours(dilated,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
    
    images = []
    
    # split columns
    
    for cnt in contours:
        x,y,w,h = cv2.boundingRect(cnt)
        cv2.rectangle(img,(x,y),(x+w,y+h),(255,255,255),0)
        cropped = img[y:y+h,x:x+w]
        images.append(cropped)
    
    return images
                            
def extractParagraphs(img):
    
    # binarize image
    
    _,imgBinarized = cv2.threshold(img, 127, 255, cv2.THRESH_BINARY)
    imgBinarized[imgBinarized == 0] = 1
    imgBinarized[imgBinarized == 255] = 0
    
    # dilation
    
    SE = cv2.getStructuringElement(cv2.MORPH_RECT,(7,7))

    dilated = cv2.dilate(imgBinarized,SE,iterations=5)
    
    contours,hierarchy = cv2.findContours(dilated,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_SIMPLE)
    
    paragraphs = []
    
    # split columns
    
    for cnt in contours:
        x,y,w,h = cv2.boundingRect(cnt)
        cv2.rectangle(img,(x,y),(x+w,y+h),(255,255,255),0)
        cropped = img[y:y+h,x:x+w]
        paragraphs.append(cropped)
    
    return paragraphs

def process(img,imgName):
    
    im1 = removeMargins(img)
    im2 = removeTables(im1)
    im3 = splitColumns(im2)
    paragraphs = []
    for a in range (0,len(im3)):
        
        im3[a] = removeMargins(im3[a])
        im3[a] = removeTables(im3[a])
        im4 = extractParagraphs(im3[a])
        for x in im4:
            paragraphs.append(x)
    
    for x in range(0,len(paragraphs)):
        
        # remove margins
        
        paragraphs[x] = removeMargins(paragraphs[x])
        
        # add uniform margins
        
        paragraphs[x] = cv2.copyMakeBorder(paragraphs[x],20,20,20,20,cv2.BORDER_CONSTANT,value=255)
        
    # ensure list is in right order
    
    paragraphs.reverse()
    
    # save images
    
    imgNum = 1
    
    for x in paragraphs:
        cv2.imwrite('%s Paragraph %s.png'%(imgName,imgNum),x)
        imgNum += 1
        
# prompt user for the original image and the chosen image name.

img = cv2.imread(input("Enter image (including filetype):"),0)
name = input("Enter image name:")

process(img,name)