package jp.techacademy.yoshihide.nishimoto.taskapp;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;

public class create_category extends AppCompatActivity {

    private EditText mcategory_edit_text;
    private Button madd_Category_button,mcancel_button;
    private Realm mRealm;
    private RealmResults<Category> mTaskRealmResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_category);

        mcancel_button = (Button)findViewById(R.id.cancel_button);
        mcancel_button.setOnClickListener(mOnCancelClickListener);
        madd_Category_button = (Button)findViewById(R.id.add_Category_button);
        madd_Category_button.setOnClickListener(mOnDateClickListener);
        mcategory_edit_text = (EditText)findViewById(R.id.category_edit_text);
        mRealm = Realm.getDefaultInstance();
    }

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Category category = new Category();

            RealmResults<Category> taskRealmResults = mRealm.where(Category.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            category.setId(identifier);
            category.setName(mcategory_edit_text.getText().toString());
            mRealm.beginTransaction();
            mRealm.copyToRealmOrUpdate(category);
            mRealm.commitTransaction();

            mRealm.close();
            finish();

        }
    };

    private View.OnClickListener mOnCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            finish();

        }
    };
}
