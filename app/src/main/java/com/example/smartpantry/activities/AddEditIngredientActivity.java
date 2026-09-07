package com.example.smartpantry.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.database.DatabaseHelper;
import com.example.smartpantry.models.PantryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddEditIngredientActivity extends AppCompatActivity {
    private DatabaseHelper db; private TextInputEditText nameInput, quantityInput, expiryInput; private AutoCompleteTextView unitInput; private int ingredientId=-1;
    private final String[] units={"pieces","grams","kg","ml","litres","cups","tablespoons","teaspoons"};
    @Override protected void onCreate(Bundle savedInstanceState){super.onCreate(savedInstanceState);setContentView(R.layout.activity_add_edit);db=new DatabaseHelper(this); ingredientId=getIntent().getIntExtra("ingredient_id",-1);setup();if(ingredientId!=-1)loadItem();}
    private void setup(){
        TextView title=findViewById(R.id.screenTitle);title.setText(ingredientId==-1?"Add Ingredient":"Edit Ingredient");
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        nameInput=findViewById(R.id.nameInput);quantityInput=findViewById(R.id.quantityInput);expiryInput=findViewById(R.id.expiryInput);unitInput=findViewById(R.id.unitInput);quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
        ArrayAdapter<String> a=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,units);unitInput.setAdapter(a);unitInput.setOnClickListener(v->unitInput.showDropDown());expiryInput.setOnClickListener(v->pickDate());findViewById(R.id.saveButton).setOnClickListener(v->save());findViewById(R.id.cancelButton).setOnClickListener(v->finish());
    }
    private void loadItem(){PantryItem i=db.getPantryItemById(ingredientId);if(i==null){Snackbar.make(nameInput,"Ingredient no longer exists",Snackbar.LENGTH_LONG).show();finish();return;}nameInput.setText(i.getName());quantityInput.setText(String.valueOf(i.getQuantity()));unitInput.setText(i.getUnit(),false);expiryInput.setText(i.getExpiryDate());}
    private void pickDate(){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->expiryInput.setText(String.format(java.util.Locale.getDefault(),"%04d-%02d-%02d",y,m+1,d)),c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private void save(){
        String name=nameInput.getText()==null?"":nameInput.getText().toString().trim();String qty=quantityInput.getText()==null?"":quantityInput.getText().toString().trim();String unit=unitInput.getText()==null?"":unitInput.getText().toString().trim();String expiry=expiryInput.getText()==null?"":expiryInput.getText().toString().trim();
        if(name.isEmpty()){nameInput.setError("Ingredient name is required.");nameInput.requestFocus();return;}if(qty.isEmpty()){quantityInput.setError("Quantity is required.");quantityInput.requestFocus();return;}
        double quantity;try{quantity=Double.parseDouble(qty);}catch(NumberFormatException e){quantityInput.setError("Enter a valid number.");quantityInput.requestFocus();return;}if(quantity<=0){quantityInput.setError("Quantity must be greater than 0.");quantityInput.requestFocus();return;}if(unit.isEmpty()){unitInput.setError("Please select a unit.");unitInput.requestFocus();return;}
        PantryItem item=new PantryItem(ingredientId,name,quantity,unit,expiry);long result;if(ingredientId==-1)result=db.insertPantryItem(item);else result=db.updatePantryItem(item);if(result!=-1){Snackbar.make(nameInput,ingredientId==-1?"Ingredient saved":"Ingredient updated",Snackbar.LENGTH_SHORT).show();finish();}else Snackbar.make(nameInput,"Unable to save ingredient",Snackbar.LENGTH_LONG).show();
    }
}
