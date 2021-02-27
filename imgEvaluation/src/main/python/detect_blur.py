from imutils import paths
import argparse
import cv2
import base64
import numpy as np

def base64ToMat(base64Img):
    # Base64 format picture to opencv mat format
    imgOriginal = base64.b64decode(base64Img)
    imgNp = np.frombuffer(imgOriginal, dtype=np.uint8)
    return cv2.imdecode(imgNp, cv2.IMREAD_UNCHANGED)

def matToBase64(img):
    return base64.b64encode(cv2.imencode('.png', img)[1]).decode()

def variance_of_laplacian(img):
    # compute the Laplacian of the image and then return the focus
    # measure, which is simply the variance of the Laplacian

    # 加载图片，转化成单通道灰度图片，使用opencv拉普拉斯方差算法计算模糊值
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    fm = cv2.Laplacian(gray, cv2.CV_64F).var()
    return fm


def detectBlurImg(base64Img):
    image = base64ToMat(base64Img)
    fm = variance_of_laplacian(image)
    text = "Not Blurry"
    # 大于等于阈值（默认100）判定为清晰，小于阈值判定为模糊
    if fm < 100:
        text = "Blurry"
    cv2.putText(image, "{}: {:.2f}".format(text, fm), (10, 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 0, 255), 3)
    return matToBase64(image)

def detectBlurValue(base64Img):
    image = base64ToMat(base64Img)
    return variance_of_laplacian(image)