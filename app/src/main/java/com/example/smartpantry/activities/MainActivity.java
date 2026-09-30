package com.example.smartpantry.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.adapters.PantryAdapter;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.models.PantryItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db; private PantryAdapter adapter; private RecyclerView recycler; private TextView emptyTitle, emptyHint; private View emptyState;
    @Override protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);setContentView(R.layout.activity_main); db=new DatabaseHelper(this); setup();}
    private void setup(){
        recycler=findViewById(R.id.pantryRecycler); emptyState=findViewById(R.id.emptyState); emptyTitle=findViewById(R.id.emptyTitle); emptyHint=findViewById(R.id.emptyHint);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter=new PantryAdapter(new PantryAdapter.Listener(){public void onEdit(PantryItem i){Intent in=new Intent(MainActivity.this,AddEditIngredientActivity.class);in.putExtra("ingredient_id",i.getId());startActivity(in);} public void onDelete(PantryItem i){confirmDelete(i);}}); recycler.setAdapter(adapter);
        FloatingActionButton fab=findViewById(R.id.addFab); fab.setOnClickListener(v->startActivity(new Intent(this,AddEditIngredientActivity.class)));
        findViewById(R.id.emptyAddButton).setOnClickListener(v->startActivity(new Intent(this,AddEditIngredientActivity.class)));
        setupBottomNav(R.id.nav_pantry);
    }
    @Override protected void onResume(){super.onResume();loadPantry();}
    private void loadPantry(){List<PantryItem> items=db.getAllPantryItems();adapter.setItems(items);boolean empty=items.isEmpty();recycler.setVisibility(empty?View.GONE:View.VISIBLE);emptyState.setVisibility(empty?View.VISIBLE:View.GONE); if(!empty){showExpiryHint(items);} }
    private void showExpiryHint(List<PantryItem> items){boolean alert=getSharedPreferences("settings",MODE_PRIVATE).getBoolean("expiry_alerts",true); if(!alert)return; for(PantryItem item:items){if(isExpiringSoon(item.getExpiryDate())){Snackbar.make(recycler,item.getName()+" "+getString(R.string.expires_soon),Snackbar.LENGTH_LONG).show();break;}}}
    private boolean isExpiringSoon(String date){try{if(date==null||date.isEmpty())return false;long days=ChronoUnit.DAYS.between(LocalDate.now(),LocalDate.parse(date,DateTimeFormatter.ISO_DATE));return days>=0&&days<=3;}catch(Exception e){return false;}}
    private void confirmDelete(PantryItem item){new AlertDialog.Builder(this).setTitle("Delete ingredient?").setMessage("Remove "+item.getName()+" from your pantry?").setNegativeButton(R.string.cancel,null).setPositiveButton("Delete",(d,w)->{if(db.deletePantryItem(item.getId())>0){loadPantry();Snackbar.make(recycler,"Ingredient deleted",Snackbar.LENGTH_SHORT).show();}}).show();}
    private void setupBottomNav(int selected){BottomNavigationView nav=findViewById(R.id.bottomNavigation);nav.setSelectedItemId(selected);nav.setOnItemSelectedListener(item->{if(item.getItemId()==R.id.nav_pantry)return true;if(item.getItemId()==R.id.nav_recipes){startActivity(new Intent(this,SuggestedRecipesActivity.class));return true;}if(item.getItemId()==R.id.nav_settings){startActivity(new Intent(this,SettingsActivity.class));return true;}return false;});}
}
