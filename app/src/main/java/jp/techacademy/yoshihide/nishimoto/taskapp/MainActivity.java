package jp.techacademy.yoshihide.nishimoto.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public final static String EXTRA_TASK = "jp.techacademy.yoshihide.nishimoto.taskapp.TASK";

    private Realm mRealm;
    private RealmResults<Task> mTaskRealmResults;
    private RealmResults<Category> mTaskRealmResults2;

    //Realm(Task)のチェンジリスナー
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };

    //Realm(Category)のチェンジリスナー
    private RealmChangeListener mRealmListener2 = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadCategorylistView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;
    private Button mSearchButton;
    private ArrayList<String> categoryList = new ArrayList<String>();
    private String selected_category = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        mSearchButton = (Button)findViewById(R.id.button);
        mSearchButton.setOnClickListener(new View.OnClickListener(){
           @Override
           public void onClick(View view){
               if(selected_category.equals("")) {
                   mTaskRealmResults = mRealm.where(Task.class).findAll();
                   Log.d("mCategoryEdit","none:'" + selected_category + "'");
               } else {
                   mTaskRealmResults =  mRealm.where(Task.class).equalTo("category", selected_category).findAll();
                   Log.d("mCategoryEdit","exist:'" + selected_category + "'");
               }
               mTaskRealmResults.sort("date", Sort.DESCENDING);
               reloadListView();
           }
        });


        // Realmの設定
        // Task
        mRealm = Realm.getDefaultInstance();
        mTaskRealmResults = mRealm.where(Task.class).findAll();
        mTaskRealmResults.sort("date", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener);

        // Category
        mTaskRealmResults2 = mRealm.where(Category.class).findAll();
        mTaskRealmResults2.sort("id", Sort.DESCENDING);
        mRealm.addChangeListener(mRealmListener2);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task);

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する
                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
        reloadCategorylistView();

    }

    private void reloadListView() {

        ArrayList<Task> taskArrayList = new ArrayList<>();

        for (int i = 0; i < mTaskRealmResults.size(); i++) {
            if (!mTaskRealmResults.get(i).isValid()) continue;

            Task task = new Task();

            task.setId(mTaskRealmResults.get(i).getId());
            task.setTitle(mTaskRealmResults.get(i).getTitle());
            task.setContents(mTaskRealmResults.get(i).getContents());
            task.setCategory(mTaskRealmResults.get(i).getCategory());
            task.setCategoryId(mTaskRealmResults.get(i).getCategoryId());
            task.setDate(mTaskRealmResults.get(i).getDate());

            taskArrayList.add(task);
        }

        mTaskAdapter.setTaskArrayList(taskArrayList);
        mListView.setAdapter(mTaskAdapter);
        mTaskAdapter.notifyDataSetChanged();
    }

    private void reloadCategorylistView() {

        categoryList.clear();
        categoryList.add("");

        for (int i = 0; i < mTaskRealmResults2.size(); i++) {
            if (!mTaskRealmResults2.get(i).isValid()) continue;
            Log.d("reload","count" + String.valueOf(mTaskRealmResults2.size()));

            categoryList.add(mTaskRealmResults2.get(i).getName());

            // Adapterの作成
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, categoryList);
            // ドロップダウンのレイアウトを指定
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // ListViewにAdapterを関連付ける
            Spinner spinner = (Spinner) findViewById(R.id.spinner1);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    Spinner spinner = (Spinner) parent;
                    // 選択されたアイテムを取得します
                    selected_category = (String) spinner.getSelectedItem();
                    Log.d("selected",selected_category);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

}
