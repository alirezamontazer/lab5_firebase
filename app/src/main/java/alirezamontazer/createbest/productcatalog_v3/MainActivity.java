package alirezamontazer.createbest.productcatalog_v3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;
    List<Product> products;
    DatabaseReference databaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        listViewProducts = findViewById(R.id.listViewProducts);
        buttonAddProduct = findViewById(R.id.addButton);

        databaseProducts = FirebaseDatabase.getInstance().getReference("products");

        products = new ArrayList<>();

        clickOnAddProduct();
        onItemLongClick();

    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                products.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Product product = postSnapshot.getValue(Product.class);
                    products.add(product);
                }

                ProductList productsAdapter = new ProductList(MainActivity.this, products);
                listViewProducts.setAdapter(productsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void clickOnAddProduct() {
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });
    }

    private void onItemLongClick() {

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });
    }

    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = dialogView.findViewById(R.id.dialog_editTextName);
        final EditText editTextPrice = dialogView.findViewById(R.id.dialog_editTextPrice);
        final Button buttonUpdate = dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(view -> {
            String name = editTextName.getText().toString().trim();
            double price = Double.parseDouble(editTextPrice.getText().toString());
            if (!TextUtils.isEmpty(name)) {
                updateProduct(productId, name, price);
                b.dismiss();
            }
        });

        buttonDelete.setOnClickListener(view -> {
            deleteProduct(productId);
            b.dismiss();
        });
    }

    private void updateProduct(String id, String name, double price) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        Product product = new Product(id, name, price);
        dR.setValue(product);
        Toast.makeText(this, "Product Updated", Toast.LENGTH_SHORT).show();
    }

    private void deleteProduct(String id) {
        DatabaseReference dR = FirebaseDatabase.getInstance().getReference("products").child(id);
        dR.removeValue();
        Toast.makeText(getApplicationContext(), "Product Deleted", Toast.LENGTH_LONG).show();
    }

    private void addProduct() {
        String name = editTextName.getText().toString().trim();
        String price = editTextPrice.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(price)) {

            String id = databaseProducts.push().getKey();
            Double priceDouble = Double.parseDouble(price);

            if (id != null) {
                Product product = new Product(id, name, priceDouble);
                databaseProducts.child(id).setValue(product);
                editTextName.setText("");
                editTextPrice.setText("");
            }

            Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Make sure all fields are filled!", Toast.LENGTH_SHORT).show();
        }

    }


}