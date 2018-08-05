package ynwa.guideme.visitor.category;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;

public class CategoryModel implements Parcelable {

    private String id, name;
    private StorageReference imageReference;

    public CategoryModel() {
    }

    public CategoryModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryModel(String id, String name, StorageReference imageReference) {
        this.id = id;
        this.name = name;
        this.imageReference = imageReference;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public StorageReference getImageReference() {
        return imageReference;
    }

    @Override
    public String toString() {
        return this.name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
    }

    private CategoryModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
    }

    public static final Parcelable.Creator<CategoryModel> CREATOR = new Parcelable.Creator<CategoryModel>() {
        @Override
        public CategoryModel createFromParcel(Parcel source) {
            return new CategoryModel(source);
        }

        @Override
        public CategoryModel[] newArray(int size) {
            return new CategoryModel[size];
        }
    };
}
