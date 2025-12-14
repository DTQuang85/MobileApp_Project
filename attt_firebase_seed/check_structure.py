import firebase_admin
from firebase_admin import credentials, firestore

# Init Firebase
cred = credentials.Certificate("serviceAccount.json")
try:
    firebase_admin.initialize_app(cred)
except:
    pass

db = firestore.client()

# Lấy 5 documents đầu tiên để xem cấu trúc
collection_ref = db.collection('behavioral_questions')
docs = collection_ref.limit(5).stream()

print("📊 Cấu trúc dữ liệu trong Firebase:\n")
for doc in docs:
    data = doc.to_dict()
    print(f"Document ID: {doc.id}")
    print(f"Fields: {list(data.keys())}")
    print(f"Sample data: {data}")
    print("-" * 80)
