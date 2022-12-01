#======================================================================
# Low Pass Filter - Ideal, Butterworth, Gaussian
# lpfilter(M,N,type,radius,order)

# Arguments:
# M, N:   Image size (spatial resolution)
# type:   Filter type, you can choose from one of the following three:
#            a) Ideal ("ideal")
#            b) Butterworth ("btw")
#            c) Gaussian ("gaussian")
# radius: Cut-off radius
# order:  Only applicable to Butterworth filter

# Example:
# Ideal Low Pass Filter with Cut-off Radius of 50:
# lpfilter(500,500,"ideal",50)
#
# Butterworth Low Pass Filter with Cut-off Radius of 50 and Order of 5:
# lpfilter(500,500,"btw",50,5)
#
# Gaussian Low Pass Filter with Cut-off Radius of 50:
# lpfilter(500,500,"gaussian",50)

#======================================================================
import numpy as np

def lpfilter(M,N,*args):

    u = list(range(0,M))
    v = list(range(0,N))
    
    idx_u = []
    for i in range(0,len(u)):
        if u[i] > M/2:
            idx_u.append(i)
    
    idx_v = []
    for i in range(0,len(v)):
        if v[i] > N/2:
            idx_v.append(i)
            
    for i in range(0,len(idx_u)):
        u[idx_u[i]] = u[idx_u[i]] - M
    
    for i in range(0,len(idx_v)):
        v[idx_v[i]] = v[idx_v[i]] - N
    
    [V, U] = np.meshgrid(v,u)
    
    D = np.fft.fftshift(np.sqrt(np.power(U,2) + np.power(V,2)))
    
    H = np.zeros((M,N),dtype=np.float64)
    if len(args) < 2:
        print ("Unknown Filter Type / Radius!")
    elif args[0] == "ideal":
            for x in range(0,M):
                for y in range(0,N):
                    if D[x,y] <= args[1]:
                        H[x,y] = 1
    elif args[0] == "btw":
            H = 1 / (1 + np.power((D / args[1]),(2 * args[2])))
    elif args[0] == "gaussian":        
            H = np.exp(-(np.power(D,2)) / (2 * np.power(args[1],2)))
    else:
        print ("Unknown Filter Type / Radius!")

    return H

#=======================================================================
# Band Reject Filter - Ideal, Butterworth, Gaussian
# brfilter(M,N,type,radius,order,width)

# Arguments:
# M, N:   Image size (spatial resolution)
# type:   Filter type, you can choose from one of the following three:
#            a) Ideal ("ideal")
#            b) Butterworth ("btw")
#            c) Gaussian ("gaussian")
# radius: radius
# order:  Only applicable to Butterworth filter
# width:  The width of the stop band

# Example:
# Ideal Band Reject Filter with Radius of 50 and Width of 10:
# brfilter(500,500,"ideal",50,10)
#
# Butterworth Band Reject Filter with Radius of 50, Order of 5 and Width of 10:
# brfilter(500,500,"btw",50,5,10)
#
# Gaussian Band Reject Filter with Radius of 50 and Width of 10:
# brfilter(500,500,"gaussian",50,10)

def brfilter(M,N,*args):

    u = list(range(0,M))
    v = list(range(0,N))
    
    idx_u = []
    for i in range(0,len(u)):
        if u[i] > M/2:
            idx_u.append(i)
    
    idx_v = []
    for i in range(0,len(v)):
        if v[i] > N/2:
            idx_v.append(i)
            
    for i in range(0,len(idx_u)):
        u[idx_u[i]] = u[idx_u[i]] - M
    
    for i in range(0,len(idx_v)):
        v[idx_v[i]] = v[idx_v[i]] - N
    
    [V, U] = np.meshgrid(v,u)
    
    D = np.fft.fftshift(np.sqrt(np.power(U,2) + np.power(V,2)))
    
    if len(args) < 3:
        print ("Unknown Filter Type / Radius / Width!")
    elif args[0] == "ideal":
        Hlp = lpfilter(M,N,'ideal',args[1]-round(args[2]/2))
        Hhp = 1 - lpfilter(M,N,'ideal',args[1]+round(args[2]/2))
        Hbr = Hlp + Hhp
    elif args[0] == "btw":
        Hbr = 1 / (1 + np.power(((D*args[3]) / (np.power(D,2)-np.power(args[1],2))),2*args[2]))
    elif args[0] == "gaussian":
        K = (np.power(D,2)-np.power(args[1],2)) / (D*args[2])
        Hbr = 1-np.exp(-0.5*(np.power(K,2)))
    else:
        print ("Unknown Filter Type / Radius / Width!")
    
    return Hbr

#========================================================================
# Notch Filter - Ideal Only
# brfilter(M,N,type,radius,row_coordinate,col_coordinate)

# Arguments:
# M, N:   Image size (spatial resolution)
# type:   Only Ideal at this point
# radius: The radius of the circle
# row_coordinate: the row coordinate of the noise component
# col_coordinate: the col coordinate of the noise component

# Example:
# Ideal Notch Filter with Radius of 10, located at Coordinate (100,100):
# ntfilter(500,500,"ideal",10,100,100)

def ntfilter(M,N,*args):
    if len(args) < 4:
        print ("Unknown Filter Type / Radius / X,Y Coordinate!")
    elif args[0] != "ideal":
        print ("Only Ideal Type!")
    else:
        filter_type = args[0]
        radius = args[1]
        row_coordinate = args[2]
        col_coordinate = args[3]
        
        H1 = 1 - lpfilter(M,N,filter_type,radius)
        
        [nrow,ncol] = H1.shape
        centerCol = int(nrow/2)
        centerRow = int(ncol/2)
        
        shiftCol = col_coordinate - centerCol
        shiftRow = row_coordinate - centerRow
        
        H1 = np.roll(H1,shiftCol,axis=1)
        H1 = np.roll(H1,shiftRow,axis=0)
        
        H2 = 1 - lpfilter(M,N,filter_type,radius)
        
        shiftCol = -1 * shiftCol
        shiftRow = -1 * shiftRow
        
        H2 = np.roll(H2,shiftCol,axis=1)
        H2 = np.roll(H2,shiftRow,axis=0)
        
        H = H1 + H2 - 1
        
        return H
        
        
        
