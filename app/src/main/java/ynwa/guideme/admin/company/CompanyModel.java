package ynwa.guideme.admin.company;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.storage.StorageReference;

public class CompanyModel implements Parcelable {
    private double latitude, longitude;
    private String key, road, name, contact, category, prices, description;
    private StorageReference storageReference;

    public CompanyModel() {
    }

    public CompanyModel(double latitude, double longitude, String road) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.road = road;
    }

    public CompanyModel(double latitude, double longitude, String road, String name, String contact, String category, String prices, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.road = road;
        this.name = name;
        this.contact = contact;
        this.category = category;
        this.prices = prices;
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getRoad() {
        return road;
    }

    public String getName() {
        return name;
    }

    public String getContact() {
        return contact;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrices() {
        return prices;
    }

    public String getDescription() {
        return description;
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.key);
        dest.writeString(this.road);
        dest.writeString(this.name);
        dest.writeString(this.contact);
        dest.writeString(this.category);
        dest.writeString(this.description);
    }

    private CompanyModel(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.key = in.readString();
        this.road = in.readString();
        this.name = in.readString();
        this.contact = in.readString();
        this.category = in.readString();
        this.description = in.readString();
    }

    public static final Parcelable.Creator<CompanyModel> CREATOR = new Parcelable.Creator<CompanyModel>() {
        @Override
        public CompanyModel createFromParcel(Parcel source) {
            return new CompanyModel(source);
        }

        @Override
        public CompanyModel[] newArray(int size) {
            return new CompanyModel[size];
        }
    };
}
