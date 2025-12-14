import json
import firebase_admin
from firebase_admin import credentials, firestore

# ---------------------------------------------------
# INIT FIREBASE
# ---------------------------------------------------
cred = credentials.Certificate("serviceAccount.json")
firebase_admin.initialize_app(cred)
db = firestore.client()


# ---------------------------------------------------
# IMPORT FUNCTION
# ---------------------------------------------------
def import_json(collection_name, file_path, id_field="id"):
    print(f"\n📥 Importing {file_path} → collection '{collection_name}' ...")

    # Tải JSON
    try:
        with open(file_path, "r", encoding="utf-8-sig") as f:
            data = json.load(f)
    except Exception as e:
        print(f"❌ Lỗi đọc JSON: {e}")
        return

    if not isinstance(data, list):
        print("❌ File JSON phải là dạng LIST [].")
        return

    imported = 0

    for item in data:

        # Lấy ID từ trường id
        doc_id = item.get(id_field)

        if doc_id is None:
            print("⚠️ Bỏ qua item không có 'id':", item)
            continue

        # Đảm bảo ID dạng string
        doc_id = str(doc_id)

        try:
            db.collection(collection_name).document(doc_id).set(item)
            imported += 1
        except Exception as e:
            print(f"⚠️ Lỗi import doc ID {doc_id}: {e}")

    print(f"✅ Đã import {imported} documents vào '{collection_name}' thành công!")


# ---------------------------------------------------
# MAIN
# ---------------------------------------------------
if __name__ == "__main__":
    print("\n🚀 BẮT ĐẦU IMPORT DATA...\n")

    import_json("behavioral_questions", "behavioral_questions.json")

    print("\n🎉 IMPORT HOÀN TẤT!\n")
