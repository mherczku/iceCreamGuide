package hu.hm.icguide

import co.zsmb.rainbowcake.test.base.PresenterTest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import hu.hm.icguide.interactors.FirebaseInteractor
import hu.hm.icguide.interactors.SystemInteractor
import hu.hm.icguide.models.Shop
import hu.hm.icguide.ui.detail.DetailPresenter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailPresenterTest : PresenterTest() {

    private val systemInteractor: SystemInteractor = mock()
    private val firebaseInteractor: FirebaseInteractor = mock()
    private val detailPresenter = DetailPresenter(systemInteractor, firebaseInteractor)

    companion object {
        private val MOCK_SHOP = Shop(
            id = "test000",
            name = "Test Shop",
            geoPoint = GeoPoint(10.0, 10.0),
            address = "Test utca 23, 1154 Budapest, Hungary",
            rate = 4.5F,
            ratings = 7,
            photo = "https://cdn3.vectorstock.com/i/1000x1000/04/77/facade-of-ice-cream-shop-flat-vector-15200477.jpg"
        )


    }

    @Test
    fun `internet check uses SystemInteractor`() = runBlockingTest {
        whenever(systemInteractor.isInternetAvailable()) doReturn true

        val result = detailPresenter.isNetAvailable()

        verify(systemInteractor).isInternetAvailable()
        Assert.assertEquals(true, result)
    }


    @Test
    fun `firebaseInteractor test 1`() = runBlockingTest {
        whenever(detailPresenter.getShop("test000")) doReturn MOCK_SHOP

        val result = detailPresenter.getShop("test000").name
        verify(detailPresenter).getShop("test000").name
        Assert.assertEquals("Test Shop", result)
    }
}