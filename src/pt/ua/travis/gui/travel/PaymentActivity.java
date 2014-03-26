package pt.ua.travis.gui.travel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import static android.view.ViewGroup.*;

import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.beardedhen.androidbootstrap.BootstrapButton;
import pt.ua.travis.R;
import pt.ua.travis.core.Client;
import pt.ua.travis.core.Taxi;
import pt.ua.travis.db.PersistenceManager;
import pt.ua.travis.gui.main.MainClientActivity;
import pt.ua.travis.gui.taxichooser.TaxiItem;
import pt.ua.travis.utils.Keys;


/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>))
 * @version 1.0
 */
public class PaymentActivity extends SherlockActivity {

    private Taxi taxiToPay;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_activity);
        getSupportActionBar().hide();

        taxiToPay = (Taxi) getIntent().getSerializableExtra(Keys.TAXI_TO_PAY);
    }

    public void onNormalPayButtonClicked(View view) {
        final Client thisClient = PersistenceManager.selectThisClientAccount();

        final TextView taxiName = new TextView(getApplicationContext());
        taxiName.setText(taxiToPay.realName);
        taxiName.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        final RatingBar ratingBar = new RatingBar(getApplicationContext());
        ratingBar.setRating(0);
        ratingBar.setStepSize(0.5f);
        ratingBar.setNumStars(5);
        ratingBar.setIsIndicator(false);
        ratingBar.setMax(5);
        ratingBar.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        final ToggleButton favoriteToggle = new ToggleButton(getApplicationContext());
        favoriteToggle.setTextOn("This taxi is a favorite!");
        favoriteToggle.setTextOff("Set as favorite");
        favoriteToggle.setChecked(thisClient.favorites.contains(taxiToPay.id));
        favoriteToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(favoriteToggle.isChecked()){
                    Log.e("h", "ADDED FAVORITE");
                    thisClient.favorites.add(taxiToPay.id);
                } else {
                    Log.e("h", "REMOVED FAVORITE");
                    Integer taxiID = taxiToPay.id;
                    thisClient.favorites.remove(taxiID);
                }
            }
        });
        favoriteToggle.setLayoutParams(new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        LinearLayout parent = new LinearLayout(this);
        parent.setGravity(Gravity.CENTER);
        parent.setLayoutParams(new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        parent.setOrientation(LinearLayout.VERTICAL);
        parent.addView(taxiName);
        parent.addView(ratingBar);
        parent.addView(favoriteToggle);


        new AlertDialog.Builder(this)
                .setTitle("Rate this taxi")
                .setView(parent)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        float rating = ratingBar.getRating();
                        dialog.dismiss();
                        Log.e("RATING", rating + "");
                        taxiToPay.ratings.add(rating);
                        goToTaxiChooserActivity();
                    }
                })
                .setNegativeButton("Don't rate", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToTaxiChooserActivity();
                    }
                })
                .show();

//        final Dialog rankDialog = new Dialog(this);
//        rankDialog.setContentView(R.layout.ranking_dialog);
//        rankDialog.setCancelable(false);
//        final RatingBar ratingBar = (RatingBar)rankDialog.findViewById(R.id.dialog_ratingbar);
//
//        TextView text = (TextView) rankDialog.findViewById(R.id.dialog_taxi_name);
//        text.setText(taxiToPay.realName);
//
//        BootstrapButton submitButton = (BootstrapButton) rankDialog.findViewById(R.id.submit_button);
//        submitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rankDialog.dismiss();
//                taxiToPay.addRating(ratingBar.getRating());
//                goToTaxiChooserActivity();
//            }
//        });
//
//        BootstrapButton noSubmitButton = (BootstrapButton) rankDialog.findViewById(R.id.no_submit_button);
//        noSubmitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rankDialog.dismiss();
//                goToTaxiChooserActivity();
//            }
//        });
//        rankDialog.show();
    }

    public void onWalletPayButtonClicked(View view){

    }

    public void goToTaxiChooserActivity(){
        Intent intent = new Intent(this, MainClientActivity.class);
        startActivity(intent);
    }
}