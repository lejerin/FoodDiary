package lej.happy.fooddiary.data.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import lej.happy.fooddiary.data.remote.model.Document;

public class LocalMapData {

    @SerializedName("documents")
    @Expose
    private List<Document> documents = null;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }



}

