package com.stampur.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;


//TODO: type variable implementation is extremely ghetto
// fix it in any way you please, so long as type functionality is supported
// used in Adapter_Message, getView()

public class Stamp implements Parcelable, Comparable<Stamp> {

    private String id; //Stamp ID
    private String label; //Stamp Title
    private Category category; //Stamp Category
    private String description; //Stamp Description
    //private String type; //Stamp Type - public/private/etc
    private int numUsers; //users who have stamp

    public enum Category {
        SPECIAL("special"),
        PEOPLE("people"),
        GENERAL("general"),
        GROUPS("groups"),
        PLACES("places"),
        INTERESTS("interests"),
        ACADEMICS("academics"),
        MISC("misc");

        private final String name;
        Category(String category){ this.name = category; }
        @Override
        public String toString(){ return this.name; }
    }

/*---CONSTRUCTORS-------------------------------------------------------------------------------------*/

    public Stamp(String id, String label) {
        this(id, label, "", Category.GENERAL, -666);
    }

    public Stamp(String id, String label, String description) {
        this(id, label, description, Category.GENERAL, -666);
    }

    public Stamp(String id, String label, String description, Category category, int numUsers){
        this.id = id;
        this.label = label;
        this.category = category;
        this.description = description;
        this.numUsers = numUsers;
        //if(this.label.equals("Public")) this.type = "STAMPUR";
        //else this.type = "NORMAL";
    }

    public Stamp(Stamp s){
    	this.id = s.getId();
    	this.label = s.getLabel();
        this.category = s.getCategory();
        this.description = s.getDescription();
        //this.type = s.getType();
    }

    private Stamp(Parcel in) {
        String[] parceledArray = new String[4];
        in.readStringArray(parceledArray);
        this.id = parceledArray[0];
        this.label = parceledArray[1];
        this.category = Category.valueOf(parceledArray[2].toUpperCase());
        this.numUsers = Integer.valueOf(parceledArray[3]);
    }

/*---ACCESSORS-------------------------------------------------------------------------------------*/

    public String getId(){ return id; }
    public String getLabel(){ return label; }
    public Category getCategory(){ return category; }
    public String getDescription(){ return description; }
    //public String getType(){ return type; }
    public int getNumUsers(){ return numUsers; }

    public void setId(String id){ this.id = id; }
    public void setLabel(String label){ this.label = label; }
    public void setCategory(Category category){ this.category = category; }
    public void setDescription(String description){ this.description = description; }
    public void setNumUsers(int numUsers){ this.numUsers = numUsers; }

/*---OTHER-----------------------------------------------------------------------------------------*/

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        java.lang.String[] parceledArray = new java.lang.String[] { this.id,
                this.label,
                this.category.toString().toUpperCase(),
                Integer.toString(this.numUsers) };
        parcel.writeStringArray(parceledArray);
    }

    public static final Parcelable.Creator<Stamp> CREATOR = new Parcelable.Creator<Stamp>() {
        public Stamp createFromParcel(Parcel in) {
            return new Stamp(in);
        }

        public Stamp[] newArray(int size) {
            return new Stamp[size];
        }
    };

    @Override
    public String toString() {
        return this.label;
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof Stamp) && (this.id.equalsIgnoreCase(((Stamp) object).getId()) ||
                this.label.equalsIgnoreCase(((Stamp) object).getLabel()));
    }

    public int compareTo(Stamp stamp) {
        return this.label.toLowerCase().compareTo(stamp.getLabel().toLowerCase());
    }

  //Convert Stamp Object to JSON Object
    public JSONObject toJSONObject() {
        JSONObject out = null;
        try {
        	out = new JSONObject();
			out.put("id", this.id);
			out.put("label", this.label);
	    	out.put("category", this.label.toString());
	    	out.put("description", this.description);
		} catch (JSONException e) {e.printStackTrace();	}
        return new JSONObject();
    }
}