from imutils import paths
import cv2
import numpy as np
import base64

def base64ToMat(base64Img):
    # Base64 format picture to opencv mat format
    imgOriginal = base64.b64decode(base64Img)
    imgNp = np.frombuffer(imgOriginal, dtype=np.uint8)
    return cv2.imdecode(imgNp, cv2.IMREAD_UNCHANGED)

def matToBase64(img):
    return base64.b64encode(cv2.imencode('.png', img)[1]).decode()

def get_saturation(image):
    hsv = cv2.cvtColor(image, cv2.COLOR_RGB2HSV)
    H, S, V = cv2.split(hsv)

    # v = V.ravel()[np.flatnonzero(V)]  # 亮度非零的值
    # average_v = sum(v) / len(v)  # 平均亮度
    s = S.ravel()[np.flatnonzero(S)]  # 饱和度非零的值
    if len(s)==0:
        return -1
    average_s = sum(s) / len(s)  # 平均饱和度
    fm = average_s
    fm = np.mean(np.log(fm))
    return fm

def getSaturationImg(base64Img):
    image = base64ToMat(base64Img)
    fm = get_saturation(image)
    text= 'saturation'  #饱和度
    cv2.putText(image, "{}: {:.2f}".format(text, fm), (10, 30),
                cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 3)
    return matToBase64(image)

def getSaturationValue(base64Img):
    image = base64ToMat(base64Img)
    return get_saturation(image)