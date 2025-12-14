import firebase_admin
from firebase_admin import credentials, firestore

# Init Firebase
cred = credentials.Certificate("serviceAccount.json")
try:
    firebase_admin.initialize_app(cred)
except:
    pass

db = firestore.client()

# Đếm documents trong collection behavioral_questions
collection_ref = db.collection('behavioral_questions')
docs = collection_ref.stream()

count = 0
ids = []
for doc in docs:
    count += 1
    ids.append(doc.id)

print(f"📊 Tổng số documents trong 'behavioral_questions': {count}")
print(f"📝 IDs: {sorted([int(id) for id in ids if id.isdigit()])[:10]}... (showing first 10)")
