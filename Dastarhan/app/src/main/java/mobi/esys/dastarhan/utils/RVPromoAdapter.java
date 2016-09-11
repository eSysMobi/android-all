package mobi.esys.dastarhan.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mobi.esys.dastarhan.BaseFragment.FragmentNavigation;
import mobi.esys.dastarhan.Constants;
import mobi.esys.dastarhan.CurrentRestaurantFragment;
import mobi.esys.dastarhan.DastarhanApp;
import mobi.esys.dastarhan.R;
import mobi.esys.dastarhan.database.Food;
import mobi.esys.dastarhan.database.Promo;
import mobi.esys.dastarhan.database.RealmComponent;
import mobi.esys.dastarhan.database.Restaurant;

/**
 * Created by ZeyUzh on 19.05.2016.
 */
public class RVPromoAdapter extends RecyclerView.Adapter<RVPromoAdapter.PromoViewHolder> {
    private Context mContext;
    private RealmComponent component;
    private List<Promo> promos;
    private List<Restaurant> restaurants;
    private String locale;
    private List<String> gifts_names;
    private List<String> conditions_names;
    private FragmentNavigation navigation;

    //constructor
    public RVPromoAdapter(Context mContext, FragmentNavigation navigation, DastarhanApp dastarhanApp, String locale) {
        this.mContext = mContext;
        this.navigation = navigation;
        component = dastarhanApp.realmComponent();
        promos = component.promoRepository().getAll();
        restaurants = component.restaurantRepository().getAll();
        this.locale = locale;
        gifts_names = new ArrayList<>();
        conditions_names = new ArrayList<>();
    }

    //preparing ViewHolder
    public static class PromoViewHolder extends RecyclerView.ViewHolder {
        TextView tvPromoHeader;
        ImageView ivPromoRestaurant;
        TextView tvPromoRestaurant;
        ImageView ivPromoRestaurantRating;
        TextView tvPromoAfflicted;
        TextView tvPromoAfflicted_0;
        TextView tvPromoTimeOfActionDays;
        TextView tvPromoTimeOfActionExact;
        TextView tvPromoConditionsExact;
        int restaraunt_id = 0;

        PromoViewHolder(View itemView) {
            super(itemView);
            tvPromoHeader = (TextView) itemView.findViewById(R.id.tvPromoHeader);
            ivPromoRestaurant = (ImageView) itemView.findViewById(R.id.ivPromoRestaurant);
            tvPromoRestaurant = (TextView) itemView.findViewById(R.id.tvPromoRestaurant);
            ivPromoRestaurantRating = (ImageView) itemView.findViewById(R.id.ivPromoRestaurantRating);
            tvPromoAfflicted = (TextView) itemView.findViewById(R.id.tvPromoAfflicted);
            tvPromoAfflicted_0 = (TextView) itemView.findViewById(R.id.tvPromoAfflicted_0);
            tvPromoTimeOfActionDays = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionDays);
            tvPromoTimeOfActionExact = (TextView) itemView.findViewById(R.id.tvPromoTimeOfActionExact);
            tvPromoConditionsExact = (TextView) itemView.findViewById(R.id.tvPromoConditionsExact);
        }
    }

    @Override
    public RVPromoAdapter.PromoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_promo_recyclerview, viewGroup, false);
        PromoViewHolder promoViewHolder = new PromoViewHolder(v);
        return promoViewHolder;
    }

    @Override
    public void onBindViewHolder(RVPromoAdapter.PromoViewHolder viewHolder, int i) {
        Promo promo = promos.get(i);
        viewHolder.restaraunt_id = promo.getRes_id();
        Restaurant restaurant = null;
        for (Restaurant rest : restaurants) {
            if (rest.getServer_id() == promo.getRes_id()) {
                restaurant = rest;
                break;
            }
        }

        if (restaurant != null) {
            if (locale.equals("ru")) {
                viewHolder.tvPromoRestaurant.setText(restaurant.getRu_name());
            } else {
                viewHolder.tvPromoRestaurant.setText(restaurant.getEn_name());
            }

            int restaurant_rating = restaurant.getTotal_rating();
            switch (restaurant_rating) {
                case 0:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_0));
                    break;
                case 1:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_1));
                    break;
                case 2:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_2));
                    break;
                case 3:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_3));
                    break;
                case 4:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_4));
                    break;
                case 5:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_5));
                    break;
                case 6:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_6));
                    break;
                case 7:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_7));
                    break;
                case 8:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_8));
                    break;
                case 9:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_9));
                    break;
                case 10:
                    viewHolder.ivPromoRestaurantRating.setImageDrawable(mContext.getResources().getDrawable(R.drawable.rating_10));
                    break;
            }

            //TODO download and set image
            //viewHolder.ivPromoRestaurant.setImageBitmap(...);

            //gifts
            String gift_type = promo.getGift_type();
            String gift = promo.getGift();
            int gift_condition = 0;
            switch (gift_type) {
                case Constants.GIFT_TYPE_DISCOUNT_PERCENT_ALL:
                    //Скидка в процентах на весь заказ
                    viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + "%");
                    if (gifts_names.size() <= i) {
                        gifts_names.add("0");
                    }
                    viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                    break;
                case Constants.GIFT_TYPE_DISCOUNT_AMOUNT_ALL:
                    //Скидка в рублях на весь заказ
                    viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + Constants.CURRENCY_VERY_SHORT);
                    if (gifts_names.size() <= i) {
                        gifts_names.add("0");
                    }
                    viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                    break;
                case Constants.GIFT_TYPE_DISCOUNT_PERCENT_OFFER:
                    //Скидка в процентах на блюда, обозначенные в условиях акции
                    if (promo.isGift_condition()) {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                    } else {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                    }
                    viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + "%");
                    if (gifts_names.size() <= i) {
                        gifts_names.add(getFoodNames(promo.getGift(), true));
                    }
                    viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                    break;
                case Constants.GIFT_TYPE_DISCOUNT_AMOUNT_OFFER:
                    //Скидка в рублях на блюда, обозначенные в условиях акции
                    if (promo.isGift_condition()) {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                    } else {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                    }
                    viewHolder.tvPromoHeader.setText(mContext.getString(R.string.discount) + " " + gift + Constants.CURRENCY_VERY_SHORT);
                    if (gifts_names.size() <= i) {
                        gifts_names.add(getFoodNames(promo.getGift(), true));
                    }
                    viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                    break;
                case Constants.GIFT_TYPE_GIFT_GOODS:
                    //Блюда, которые получают в подарок
                    if (promo.isGift_condition()) {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_or);
                    } else {
                        viewHolder.tvPromoAfflicted.setText(R.string.afflicted_to_and);
                    }
                    viewHolder.tvPromoHeader.setText(R.string.meal_for_gift);
                    if (gifts_names.size() <= i) {
                        gifts_names.add(getFoodNames(promo.getGift(), true));
                    }
                    viewHolder.tvPromoAfflicted_0.setText(gifts_names.get(i));
                    break;
                case Constants.GIFT_TYPE_FREE_DELIVERY:
                    //Бесплатная доставка
                    viewHolder.tvPromoHeader.setText(R.string.free_delivery);
                    if (gifts_names.size() <= i) {
                        gifts_names.add("0");
                    }
                    viewHolder.tvPromoAfflicted_0.setText(R.string.afflicted_all_order);
                    break;
                case Constants.GIFT_TYPE_HIDDEN:
                    //Скрыто
                    viewHolder.tvPromoHeader.setText(R.string.hidden);
                    if (gifts_names.size() <= i) {
                        gifts_names.add("0");
                    }
                    viewHolder.tvPromoAfflicted_0.setText(R.string.hidden);
                    break;
            }

            //days
            StringBuilder sb = new StringBuilder();
            String[] days = promo.getDays().split(",");
            for (String day : days) {
                switch (Integer.parseInt(day)) {
                    case 1:
                        sb.append(mContext.getString(R.string.day_1)).append(" ");
                        break;
                    case 2:
                        sb.append(mContext.getString(R.string.day_2)).append(" ");
                        break;
                    case 3:
                        sb.append(mContext.getString(R.string.day_3)).append(" ");
                        break;
                    case 4:
                        sb.append(mContext.getString(R.string.day_4)).append(" ");
                        break;
                    case 5:
                        sb.append(mContext.getString(R.string.day_5)).append(" ");
                        break;
                    case 6:
                        sb.append(mContext.getString(R.string.day_6)).append(" ");
                        break;
                    case 7:
                        sb.append(mContext.getString(R.string.day_7)).append(" ");
                        break;
                }
            }
            viewHolder.tvPromoTimeOfActionDays.setText(sb.toString());

            //exact data
            StringBuilder sb_exact_data = new StringBuilder();

            if (promo.isLimitedData()) {
                String date1 = promo.getDate1();
                String date2 = promo.getDate2();
                sb_exact_data
                        .append(mContext.getString(R.string.from)).append(" ").append(date1)
                        .append(" ").append(mContext.getString(R.string.to)).append(" ").append(date2);
            } else {
                sb_exact_data.append(mContext.getString(R.string.every_day));
            }

            if (promo.isLimitedTime()) {
                String time1 = promo.getTime1();
                String time2 = promo.getTime2();
                sb_exact_data
                        .append(" ").append(mContext.getString(R.string.from)).append(" ").append(time1)
                        .append(" ").append(mContext.getString(R.string.to)).append(" ").append(time2);
            } else {
                sb_exact_data.append(" ").append(mContext.getString(R.string.around_the_clock));
            }

            viewHolder.tvPromoTimeOfActionExact.setText(sb_exact_data.toString());


            //conditions
            StringBuilder sb_condition = new StringBuilder();
            int condition = promo.getCondition();
            String condition_par = promo.getCondition_par();
            String tmp = "";
            switch (condition) {
                case 1:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Сумма заказа больше").append(" ").append(condition_par).append(Constants.CURRENCY_VERY_SHORT).toString();
                        conditions_names.add(tmp);
                    }
                    break;
                case 2:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Покупка блюд").append(" ").append(getFoodNames(condition_par, false)).toString();
                        conditions_names.add(tmp);
                    }
                    break;
                case 3:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Покупка блюда").append(" ").append(getFoodNames(condition_par, false)).toString();
                        conditions_names.add(tmp);
                    }
                    break;
                case 4:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Покупка блюд из категории").append(" ").append(condition_par).toString();
                        conditions_names.add(tmp);
                    }
                    break;
                case 5:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Введён промокод").toString();
                        conditions_names.add(tmp);
                    }
                    break;
                case 6:
                    if (conditions_names.size() <= i) {
                        tmp = sb_condition.append("Без условий").toString();
                        conditions_names.add(tmp);
                    }
                    break;
            }

            viewHolder.tvPromoConditionsExact.setText(conditions_names.get(i));


            CustomClickListener customClickListener = new CustomClickListener(navigation, viewHolder.restaraunt_id);
            viewHolder.itemView.setOnClickListener(customClickListener);

            CustomLongClickListener customLongClickListener = new CustomLongClickListener(navigation, viewHolder.restaraunt_id);
            viewHolder.itemView.setOnLongClickListener(customLongClickListener);
        }
    }

    private String getFoodNames(String ids, boolean needLines) {
        String names = "";
        try {
            String[] parts = ids.split(",");
            List<Food> foods = new ArrayList<>();
            if (parts.length > 0) {
                Integer[] searchIDs = new Integer[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    searchIDs[i] = Integer.parseInt(parts[i]);
                }
                foods = component.foodRepository().getByIds(searchIDs);

            }
            if (foods.size() > 0) {
                boolean firstSeparator = false;
                for (Food food : foods) {
                    if (firstSeparator) {
                        if (needLines) {
                            names = names + System.getProperty("line.separator");
                        } else {
                            names = names + ", ";
                        }
                    }
                    if (locale.equals("ru")) {
                        String name = food.getRu_name();
                        if (name == null || name.isEmpty()) {
                            names = names + food.getEn_name();
                        } else {
                            names = names + name;
                        }
                    } else {
                        String name = food.getEn_name();
                        if (name == null || name.isEmpty()) {
                            names = names + food.getRu_name();
                        } else {
                            names = names + name;
                        }
                    }
                    firstSeparator = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (names.isEmpty()) {
            names = "Food with this ID not exists in DB!";
        }
        return names;
    }

    private static class CustomClickListener implements View.OnClickListener {
        private int id;
        private FragmentNavigation navigation;

        public CustomClickListener(FragmentNavigation navigation, int id) {
            this.navigation = navigation;
            this.id = id;
        }

        @Override
        public void onClick(View v) {
            Log.d("dtagRecyclerView", "Click PROMO in RecyclerView with id = " + id);
            //TODO link with food in promo
//            Intent intent = new Intent(mContext, FoodFragment.class);
//            intent.putExtra("restID",id);
//            intent.putExtra("cuisineID", -50);
//            mContext.startActivity(intent);
        }
    }

    private static class CustomLongClickListener implements View.OnLongClickListener {
        private int id;
        private FragmentNavigation navigation;

        public CustomLongClickListener(FragmentNavigation navigation, int id) {
            this.navigation = navigation;
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d("dtagRecyclerView", "Long click PROMO in RecyclerView with id = " + id);
            CurrentRestaurantFragment fragment = CurrentRestaurantFragment.newInstance(id);
            navigation.replaceFragment(fragment, "Ресторан");
            return true;
        }
    }

    @Override
    public int getItemCount() {
        return promos.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}