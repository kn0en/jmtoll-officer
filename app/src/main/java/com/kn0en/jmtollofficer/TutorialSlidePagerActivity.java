package com.kn0en.jmtollofficer;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TutorialSlidePagerActivity extends AppCompatActivity {

    private TutorialSlidePageAdapter tutorialSlidePageAdapter;
    private LinearLayout sliderIndicator;
    private MaterialButton buttonTutorialAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_slide);

        sliderIndicator = (LinearLayout) findViewById(R.id.slideIndicators);
        buttonTutorialAction = (MaterialButton) findViewById(R.id.buttonNext);

        setupTutorialItems();

        ViewPager2 tutorialViewPager2 = findViewById(R.id.pager);
        tutorialViewPager2.setAdapter(tutorialSlidePageAdapter);

        setupTutorialIndicators();
        setCurrentTutorialIndicator(0);

        tutorialViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentTutorialIndicator(position);
            }
        });

        buttonTutorialAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tutorialViewPager2.getCurrentItem() + 1 < tutorialSlidePageAdapter.getItemCount()){
                    tutorialViewPager2.setCurrentItem(tutorialViewPager2.getCurrentItem() + 1);
                }else {
                    finish();
                }
            }
        });
    }

    private void setupTutorialItems(){
        List<TutorialSlidePageItem> tutorialSlidePageItems = new ArrayList<>();
        TutorialSlidePageItem item1 = new TutorialSlidePageItem();
        item1.setTextTitle("TUTORIAL PENGGUNAAN APLIKASI 'JMTOLL OFFICER'");
        item1.setTextContent("1. Setelah petugas masuk aplikasi maka akan langsung di arahkan ke menu utama. " +
                "Pada menu utama terdapat fitur utama yang akan digunakan petugas yaitu Fitur 'MAPS'. " +
                "\n" +
                "2. Klik pada hamburger bar untuk melihat menu navigasi.");
        item1.setImage(R.drawable.slide1);

        TutorialSlidePageItem item2 = new TutorialSlidePageItem();
        item2.setTextContent("3. Sebelum menggunakan aplikasi ini, petugas DIHARUSKAN mengisi data diri terlebih dahulu." +
                "\n" +
                "4. Pada menu navigasi pilih fitur 'MY PROFILE' untuk merubah informasi data diri.");
        item2.setImage(R.drawable.slide2);

        TutorialSlidePageItem item2_1 = new TutorialSlidePageItem();
        item2_1.setTextContent("5. Setelah masuk fitur 'MY PROFILE', lengkapi data diri yang sudah tersedia," +
                "\n" +
                "6. Isi data dengan benar dan tekan 'CONFIRM' untuk menyimpan data.");
        item2_1.setImage(R.drawable.slide2_1);

        TutorialSlidePageItem item3 = new TutorialSlidePageItem();
        item3.setTextContent("7. Setelah selesai 'CONFIRM' data diri, maka otomatis akan langsung dialhkan ke menu utama." +
                "\n" +
                "8. Untuk menggunakan fitur utama 'MAPS', masuk lagi ke menu navigasi lalu tekan switch untuk mengatifkan mode bertugas.");
        item3.setImage(R.drawable.slide3);

        TutorialSlidePageItem item4 = new TutorialSlidePageItem();
        item4.setTextContent("9. Ketika status bertugas sudah aktif, maka aplikasi siap digunakan untuk penjemputan kendaraan yang sedang mengalami masalah." +
                "\n" +
                "10. Untuk dapat mengetahui lokasi petugas saat ini, dapat menekan icon 'LOCATION'.");
        item4.setImage(R.drawable.slide4);

        TutorialSlidePageItem item5 = new TutorialSlidePageItem();
        item5.setTextContent("11. Jika terdapat pengguna/kendaraan yang bermasalah. Maka otomatis sistem akan membuat rute jalur penjemputan serta menampilkan informasi nama pengguna, nomer telepon, golongan kendaraan serta jarak antara petugas sampai ke lokasi pengguna." +
                "\n" +
                "12. Untuk menerima permintaan penjemputan, petugas dapat menekan 'PICKED CUSTOMER'.");
        item5.setImage(R.drawable.slide5);

        tutorialSlidePageItems.add(item1);
        tutorialSlidePageItems.add(item2);
        tutorialSlidePageItems.add(item2_1);
        tutorialSlidePageItems.add(item3);
        tutorialSlidePageItems.add(item4);
        tutorialSlidePageItems.add(item5);

        tutorialSlidePageAdapter = new TutorialSlidePageAdapter(tutorialSlidePageItems);
    }

    private void setupTutorialIndicators(){
        ImageView[] indicators = new ImageView[tutorialSlidePageAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        layoutParams.setMargins(8,0,8,0);
        for (int i = 0; i < indicators.length; i++){
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.tutorial_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            sliderIndicator.addView(indicators[i]);
        }
    }

    private void setCurrentTutorialIndicator(int index){
        int childCount = sliderIndicator.getChildCount();
        for (int i = 0;  i < childCount; i++){
            ImageView imageView = (ImageView) sliderIndicator.getChildAt(i);
            if (i == index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.tutorial_indicator_active)
                );
            }else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.tutorial_indicator_inactive)
                );
            }
        }
        if (index == tutorialSlidePageAdapter.getItemCount() - 1){
            buttonTutorialAction.setText("Finish");
        }else {
            buttonTutorialAction.setText("Next");
        }
    }
}
