import requests

url = "https://github.com/DTQuang85/videoInterviewIT/releases/download/v1.1/Backend.Engineering.mp4"

# yêu cầu 1 đoạn nhỏ của video (range request)
headers = {
    "Range": "bytes=0-1024"
}

resp = requests.get(url, headers=headers, stream=True)

print("Status code:", resp.status_code)
print("Accept-Ranges:", resp.headers.get("Accept-Ranges"))
print("Content-Type:", resp.headers.get("Content-Type"))
print("Content-Length:", resp.headers.get("Content-Length"))
print("Content-Disposition:", resp.headers.get("Content-Disposition"))

# nếu status_code là 206 = OK STREAMING
if resp.status_code == 206:
    print("🎉 VIDEO STREAM ĐƯỢC — ExoPlayer phát OK.")
else:
    print("⚠ KHÔNG STREAM ĐƯỢC — Link này chỉ hỗ trợ download.")
