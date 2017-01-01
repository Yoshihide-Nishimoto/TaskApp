package jp.techacademy.yoshihide.nishimoto.taskapp;

import java.io.Serializable;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Serializable {

    // id をプライマリーキーとして設定
    @PrimaryKey
    private int id;
    private String category;

    public int getId() {

        return id;

    }

    public void setId(int id) {

        this.id = id;

    }

    public String getName() {

        return category;

    }

    public void setName(String category) {

        this.category = category;

    }

}
