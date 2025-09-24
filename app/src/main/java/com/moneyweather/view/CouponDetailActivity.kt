package com.moneyweather.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.moneyweather.R
import com.moneyweather.adapter.CouponDescPagerAdapter
import com.moneyweather.base.BaseKotlinActivity
import com.moneyweather.databinding.ActivityCouponDetailBinding
import com.moneyweather.model.CouponItem
import com.moneyweather.model.enums.ActionBarLeftButtonEnum
import com.moneyweather.model.enums.CouponStatus
import com.moneyweather.model.enums.DialogType
import com.moneyweather.model.enums.ResultCode
import com.moneyweather.model.enums.TermsType
import com.moneyweather.ui.dialog.HCCommonDialog
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.FileUtils
import com.moneyweather.util.Logger
import com.moneyweather.viewmodel.CouponDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date

@AndroidEntryPoint
class CouponDetailActivity : BaseKotlinActivity<ActivityCouponDetailBinding, CouponDetailViewModel>(), View.OnClickListener,
    OnRefreshListener {

    override val layoutResourceId: Int get() = R.layout.activity_coupon_detail
    override val viewModel: CouponDetailViewModel by viewModels()

    private var mCouponDescPagerAdapter: CouponDescPagerAdapter? = null
    private var mCouponPk: Int = 0
    private var mCouponItem: CouponItem? = null

    override fun initStartView() {

        viewDataBinding.vm = viewModel
        initActionBar(viewDataBinding.iActionBar, R.string.coupon, ActionBarLeftButtonEnum.BACK_BUTTON)

        mCouponPk = intent.getIntExtra("KEY_COUPON_PK", 0)


        initView()
        onRefresh()

        viewModel.resultCoupon.observe(this, Observer {
            viewDataBinding.apply {
                if (layRefresh.isRefreshing()) {
                    layRefresh.setRefreshing(false)
                }
                progress.setVisibility(View.GONE)
                mCouponItem = it
                dataSetChanged()
            }

        })

        viewModel.resultCouponRefund.observe(this, Observer {
            viewDataBinding.apply {

                if (it.result == ResultCode.SUCCESS.resultCode) {
                    Toast.makeText(
                        this@CouponDetailActivity,
                        R.string.coupon_refund_success,
                        Toast.LENGTH_SHORT
                    ).show()

                    mCouponItem?.status = CouponStatus.REFUND.state
                    dataSetChanged()
//                    BusProvider.getInstance()
//                        .post(CouponStateUpdate(mCouponPk, CouponStatus.REFUND.state))
//
//                    BusProvider.getInstance().post(BusEvent.ACTION_DATE_CHANGED)
                } else {
                    val msg = if (TextUtils.isEmpty(it.msg)
                    ) getString(R.string.not_connect_error) else it.msg
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }


            }

        })

    }

    fun initView() {
        viewDataBinding.apply {
            layRefresh.setOnRefreshListener(this@CouponDetailActivity)
            mCouponDescPagerAdapter = CouponDescPagerAdapter()
            pagerDetail.adapter = mCouponDescPagerAdapter;
            pagerDetail.registerOnPageChangeCallback(object : OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    val view =
                        (pagerDetail.getChildAt(0) as RecyclerView).layoutManager!!.findViewByPosition(
                            position
                        )
                    updatePagerHeightForChild(view, pagerDetail)
                }
            })
            TabLayoutMediator(
                layTab, pagerDetail
            ) { tab: TabLayout.Tab, position: Int ->
                if (position == 0) {
                    tab.setText(R.string.product_description)
                } else {
                    tab.setText(R.string.product_precautions)
                }
            }.attach()
        }
    }

    private fun updatePagerHeightForChild(view: View?, pager2: ViewPager2) {
        view?.post {
            val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                view.width,
                View.MeasureSpec.EXACTLY
            )
            val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                view.height,
                View.MeasureSpec.UNSPECIFIED
            )
            view.measure(wMeasureSpec, hMeasureSpec)
            if (pager2.layoutParams != null && pager2.layoutParams.height != view.measuredHeight) {
                pager2.layoutParams.height = view.measuredHeight
                mCouponDescPagerAdapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onClick(view: View?) {
        view?.id.let {
            val id = it
            if (id == R.id.btnRefund) {
//                openRefundConfirmDialog()
            } else if (id == R.id.btnCouponSave) {
                saveImage()
            } else if (id == R.id.btnTerms) {
                val intent = Intent(this, TermsActivity::class.java)
                intent.putExtra("type", TermsType.SERVICE)
                startActivity(intent)
            } else if (id == R.id.btnBusinessInfo) {
                val intent = Intent(this, TermsActivity::class.java)
                intent.putExtra("type", TermsType.COMPANY)
                startActivity(intent)
            }
        }

    }

    private fun openRefundConfirmDialog() {
        val dialog: HCCommonDialog = HCCommonDialog(this)
            .setDialogType(DialogType.ALERT)
            .setDialogTitle(R.string.coupon_refund_confirm_dialog_title)
            .setContent(R.string.coupon_refund_confirm_dialog_content)
            .setPositiveButtonText(R.string.confirm)
            .setNegativeButtonText(R.string.cancel)
            .setOnDismissListener(object : HCCommonDialog.OnDismissListener {
                override fun onDismiss(menuId: Int) {
                    if (menuId == DialogType.BUTTON_POSITIVE.ordinal) {
                        couponRefund()
                    }
                }
            })
        dialog.show()
    }

    @SuppressLint("StaticFieldLeak")
    private fun saveImage() {

        viewDataBinding.progress.setVisibility(View.VISIBLE)

        Thread(object : Runnable {
            var fileUri: Uri? = null

            override fun run() {
                try {
                    viewDataBinding.apply {
                        layGifticon.setDrawingCacheEnabled(true) // 화면에 뿌릴때 캐시를 사용하게 한다

                        val screenBitmap = Bitmap.createBitmap(
                            layGifticon.getWidth(),
                            layGifticon.getHeight(),
                            Bitmap.Config.ARGB_8888
                        )
                        val c = Canvas(screenBitmap)
                        layGifticon.draw(c)

                        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
                        val date = dateFormat.format(Date(System.currentTimeMillis()))

                        val fileName = "PopupCash_$date.JPEG"

                        fileUri = FileUtils.Companion.saveFile(
                            this@CouponDetailActivity,
                            screenBitmap,
                            Environment.DIRECTORY_PICTURES,
                            fileName
                        )

                        layGifticon.setDrawingCacheEnabled(false)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                }

                Handler(Looper.getMainLooper()).post(Runnable {
                    viewDataBinding.progress.setVisibility(View.GONE)
                    if (fileUri != null) {
                        Toast.makeText(
                            baseContext,
                            R.string.message_coupon_save_success,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            baseContext,
                            R.string.message_image_save_fail,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }).start()
    }

    private fun dataSetChanged() {
        // 상품 이미지
        mCouponItem?.let { mCouponItem ->
            viewDataBinding.apply {
                Glide.with(applicationContext)
                    .load(mCouponItem.goodsImageSmall)
                    .centerCrop()
                    .error(R.drawable.img_coupon_error)
                    .fallback(R.drawable.img_coupon_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgProduct)

                // 판매처
                txtPlaceForSale.setText(mCouponItem.affiliate)

                // 상품명
                txtProductName.setText(mCouponItem.goodsName)

                // 상품가격
                val price: String = CommonUtils.getCommaNumeric(mCouponItem.salePrice!!.toFloat()) + "P"
                txtProductPrice.setText(price)

                // 유효기간
                val dueDate = String.format(
                    getString(R.string.due_date_value),
                    java.lang.String.valueOf(mCouponItem.expiredDate)
                )
                txtDueDate.setText(dueDate)

                // 바코드 이미지, 텍스트
                val barcode: Bitmap? = createBarcode(mCouponItem.pinNo!!)
                if (barcode != null) {
                    imgBarcode.setImageBitmap(barcode)
                }
                txtBarcode.setText(mCouponItem!!.pinNo)

                // 쿠폰 상태에 따른 화면 처리
                val couponStatus: CouponStatus = CouponStatus.find(mCouponItem.status!!)
                if (couponStatus === CouponStatus.BUY) {
                    groupCouponStatus.setVisibility(View.GONE)
                    btnRenew.setEnabled(true)
//                    btnRefund.setEnabled(true)
                    btnCouponSave.setEnabled(true)
                } else {
                    groupCouponStatus.setVisibility(View.VISIBLE)
                    imgCouponStatus.setImageResource(couponStatus.getContentImgRes())
                    btnRenew.setEnabled(false)
//                    btnRefund.setEnabled(false)
                    btnCouponSave.setEnabled(false)
                }

                // 상품 설명, 유의사항 페이저
                val descData = ArrayList<String>()
                descData.add(mCouponItem.goodsDescription!!) // 상품설명
//                descData.add(mCouponItem.caution!!) // 유의사항
                mCouponDescPagerAdapter!!.setData(descData)


                // 기프티콘
                Glide.with(applicationContext)
                    .load(mCouponItem.goodsImageSmall)
                    .centerCrop()
                    .error(R.drawable.img_coupon_error)
                    .fallback(R.drawable.img_coupon_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imgProductForGifticon)
                txtProductNameForGifticon.setText(mCouponItem.goodsName)
                txtAffiliateForGifticon.setText(mCouponItem.affiliate)
                txtDueDateForGifticon.setText(mCouponItem.limitDay.toString().plus("일 남음"))
                if (barcode != null) {
                    imgBarcodeForGifticon.setImageBitmap(barcode)
                }
                txtBarcodeForGifticon.setText(mCouponItem.pinNo)
            }
        }


    }

    override fun onRefresh() {
        getCouponDetail()
    }

    fun getCouponDetail() {

        viewModel.connectGetCouponDetail(mCouponPk)
    }

    private fun couponRefund() {

        viewDataBinding.btnRefund.isClickable = false
        viewDataBinding.progress.visibility = View.VISIBLE

        val param = HashMap<String, Any?>()
        param["couponId"] = mCouponItem?.couponId

        viewModel.connectCouponRefund(param)
    }


    private fun createBarcode(pinCode: String): Bitmap? {
        if (TextUtils.isEmpty(pinCode)) {
            return null
        }

        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.getRealMetrics(metrics)
        } else {
            windowManager.defaultDisplay.getRealMetrics(metrics)
        }


        val margin: Float =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50f, resources.displayMetrics)
        val width = (metrics.widthPixels - (margin * 2)).toInt()
        val height = width / 5 * 2

        val format: BarcodeFormat = BarcodeFormat.CODE_128
        try {
            val matrix: BitMatrix = MultiFormatWriter().encode(pinCode, format, width, height)
            val bitmap =
                Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            return bitmap
        } catch (e: WriterException) {
            Logger.e(e.message)
            return null
        }
    }

}


