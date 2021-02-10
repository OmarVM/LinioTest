package com.example.myapplintest.usecase;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplintest.model.FavoritesCollection;
import com.example.myapplintest.model.Product;
import com.example.myapplintest.model.Users;
import com.example.myapplintest.network.INetworkFavoritesCollection;
import com.example.myapplintest.network.INetworkListFavorites;
import com.example.myapplintest.network.LinAPI;

import java.util.ArrayList;
import java.util.List;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkListFavoritesImpl implements INetworkListFavorites, INetworkFavoritesCollection {

    public NetworkListFavoritesImpl() {
        InitRequest();
    }

    private CompositeDisposable _disposables = new CompositeDisposable();
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(LinAPI.URL_MAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build();

    private LinAPI mService = retrofit.create(LinAPI.class);
    public Single<List<Users>> mListObservable = mService.getUsers();
    private MutableLiveData _mList = new MutableLiveData<List<Product>>();
    private MutableLiveData _mListCollection = new MutableLiveData<List<FavoritesCollection>>();

    private void InitRequest(){
        mListObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Users>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onSuccess(@NonNull List<Users> usersList) {
                        ArrayList<FavoritesCollection> listCollections = new ArrayList<>();

                        //Get all Collection
                        for (Users user : usersList){
                            FavoritesCollection newCollection = new FavoritesCollection();
                            newCollection.setDescription(user.description);
                            ArrayList<String> newListUrls = new ArrayList<>();
                            for (Product product: user.products.values()){
                                newListUrls.add(product.image);
                            }
                            newCollection.setListImages(newListUrls);
                            listCollections.add(newCollection);
                        }

                        //Empty collection
                        FavoritesCollection emptyCollection = new FavoritesCollection();
                        ArrayList<String> listUrlsEmpty = new ArrayList<>();
                        emptyCollection.setDescription("");
                        for (int j = 0; j<3; j++){
                            listUrlsEmpty.add("");
                        }
                        emptyCollection.setListImages(listUrlsEmpty);
                        listCollections.add(emptyCollection);

                        _mListCollection.setValue(listCollections);

                        //Get all products from services
                        ArrayList<Product> mArr = new ArrayList<>();
                        for (Users users: usersList) {
                            for (Product product : users.products.values()) {
                                mArr.add(product);
                            }
                        }
                        _mList.setValue(mArr);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }
                });

    }

    @Override
    public LiveData<List<Product>> getInfo() {
        return _mList;
    }

    @Override
    public LiveData<List<FavoritesCollection>> getInfoCollection() {
        return _mListCollection;
    }
}
