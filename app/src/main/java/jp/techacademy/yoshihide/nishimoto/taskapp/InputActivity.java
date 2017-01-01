package jp.techacademy.yoshihide.nishimoto.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class InputActivity extends AppCompatActivity {

    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton,madd_Category_button;
    private EditText mTitleEdit, mContentEdit;
    private Task mTask;
    private RealmResults<Category> mTaskRealmResults2;
    private ArrayList<String> categoryList = new ArrayList<String>();
    private String selected_category = "";
    private int selected_category_id;
    private Realm mRealm;
    private RealmChangeListener mRealmListener2 = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadCategorylistView();
        }
    };

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(InputActivity.this, create_category.class);
            startActivity(intent);

        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // Category
        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults2 = mRealm.where(Category.class).findAll();
        mTaskRealmResults2.sort("id", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener2);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button)findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button)findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        madd_Category_button = (Button)findViewById(R.id.add_Category_button);
        madd_Category_button.setOnClickListener(mOnCategoryClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        mTitleEdit = (EditText)findViewById(R.id.title_edit_text);
        mContentEdit = (EditText)findViewById(R.id.content_edit_text);

        Log.d("mTitleEdit","abc");

        Intent intent = getIntent();
        mTask = (Task) intent.getSerializableExtra(MainActivity.EXTRA_TASK);

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());
            selected_category_id = (mTask.getCategoryId());
            Log.d("judge2",String.valueOf(selected_category_id));
            selected_category = (mTask.getCategory());
            mContentEdit.setText(mTask.getContents());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d",(mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }

        reloadCategorylistView();

    }

    private void addTask() {
        Realm realm = Realm.getDefaultInstance();

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String content = mContentEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setContents(content);
        mTask.setCategory(selected_category);
        mTask.setCategoryId(selected_category_id);
        Log.d("setCategoryId",String.valueOf(selected_category_id));
        GregorianCalendar calendar = new GregorianCalendar(mYear,mMonth,mDay,mHour,mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask);
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);

    }

    private void reloadCategorylistView() {

        categoryList.clear();
        categoryList.add("");

        for (int i = 0; i < mTaskRealmResults2.size(); i++) {
            if (!mTaskRealmResults2.get(i).isValid()) continue;
            Log.d("reload2","count" + String.valueOf(selected_category_id));

            //Category category = new Category();

            // category.setId(mTaskRealmResults2.get(i).getId());
            //category.setName(mTaskRealmResults2.get(i).getName());

            categoryList.add(mTaskRealmResults2.get(i).getName());

            //categoryArrayList.add(category);

            // Adapterの作成
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, categoryList);
            // ドロップダウンのレイアウトを指定
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // ListViewにAdapterを関連付ける
            Spinner spinner = (Spinner) findViewById(R.id.spinner1);
            spinner.setAdapter(adapter);
            spinner.setSelection(selected_category_id);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    Spinner spinner = (Spinner) parent;
                    // 選択されたアイテムを取得します
                    selected_category = (String) spinner.getSelectedItem();
                    selected_category_id = (int) spinner.getSelectedItemId();
                    Log.d("selected",String.valueOf(selected_category_id));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        }

    }
}
