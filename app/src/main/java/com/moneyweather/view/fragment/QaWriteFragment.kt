package com.moneyweather.view.fragment

import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.text.format.Formatter
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.moneyweather.R
import com.moneyweather.base.BaseKotlinFragment
import com.moneyweather.databinding.FragmentQaWriteBinding
import com.moneyweather.model.enums.TermsType
import com.moneyweather.util.CommonUtils
import com.moneyweather.util.Logger
import com.moneyweather.util.RealPathUtil
import com.moneyweather.view.TermsActivity
import com.moneyweather.viewmodel.QaWriteViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.util.Locale
import java.util.regex.Pattern

@AndroidEntryPoint
class QaWriteFragment : BaseKotlinFragment<FragmentQaWriteBinding, QaWriteViewModel>() {
    override val layoutResourceId: Int get() = R.layout.fragment_qa_write
    override val viewModel: QaWriteViewModel by viewModels()

    val REQUEST_GET_CONTENT = 1245
    var mAttachedFile: File? = null


    override fun initStartView() {

        viewDataBinding.apply {
            editTitle.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    val first_str = p0.toString().length.toString()
                    val last_str = "/15"
                    val spannableString = SpannableString(first_str)
                    val builder = SpannableStringBuilder(spannableString)
                    builder.append(last_str)
                    val begin = 0
                    val end = first_str.length
                    builder.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_222)),
                        begin,
                        end,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    titleCount.text = builder
                    btnRegisterEnable()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            editQaContent.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {
                    val first_str = p0.toString().length.toString()
                    val last_str = "/500"
                    val spannableString = SpannableString(first_str)
                    val builder = SpannableStringBuilder(spannableString)
                    builder.append(last_str)
                    val begin = 0
                    val end = first_str.length
                    builder.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_222)),
                        begin,
                        end,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    contentCount.text = builder
                    btnRegisterEnable()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })

            btnFile.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                intent.type = "*/*"
                startActivityForResult(intent, REQUEST_GET_CONTENT)
            }

            btnTerms.setOnClickListener {
                btnTerms.setSelected(!btnTerms.isSelected())
                btnRegisterEnable()
            }

            btnPrivacy.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            btnPrivacy.setOnClickListener {
                val intent = Intent(getContext(), TermsActivity::class.java)
                intent.putExtra("type", TermsType.PRIVACY)
                startActivity(intent)
            }

            btnRegister.setOnClickListener {
                var m = HashMap<String, Any?>()
                m["title"] = editTitle.text.toString()
                m["content"] = editQaContent.text.toString()
                m["appVersion"] = CommonUtils.getAppVersion()
                m["device"] = Build.MODEL
                m["os"] = Build.VERSION.SDK_INT

                viewModel.writeQa(m)
            }

        }

        viewModel.complete.observe(this, Observer {
            activity?.onBackPressed()
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Timber.d("onActivityResult is call")
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            data.data?.let {
                checkFile(it)
            }
        } else {
            if (mAttachedFile == null) {
                viewDataBinding.txtFileLabel.setVisibility(View.VISIBLE)
                viewDataBinding.txtFileName.setVisibility(View.GONE)
            }
        }
    }

    private fun checkFile(returnUri: Uri) {

        // create file
        val realPath: String
        // SDK < API11
        realPath = if (Build.VERSION.SDK_INT < 11) {
            RealPathUtil.getRealPathFromURI_BelowAPI11(requireContext(), returnUri)
        } else if (Build.VERSION.SDK_INT < 19) {
            RealPathUtil.getRealPathFromURI_API11to18(requireContext(), returnUri)
        } else {
            RealPathUtil.getRealPathFromURI_API19(requireContext(), returnUri)
        }
        if (!TextUtils.isEmpty(realPath)) {
            mAttachedFile = File(realPath)
        }

        // 파일 생성되지 않았을 경우 return
        if (mAttachedFile == null) {
            Toast.makeText(
                requireContext(),
                R.string.qa_message_file_reselect,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        mAttachedFile?.let {
            val maxSize: Long = 1048576 // 1MB
            val fileIndex: Int = it.getName().lastIndexOf(".")
            val mimeType: String = it.getName().substring(fileIndex + 1)
            Logger.v("mimeType $mimeType")
            activity?.let {
                val returnCursor: Cursor? = it.getContentResolver().query(returnUri, null, null, null, null)
                returnCursor?.let {
                    it.moveToFirst()
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    val name = it.getString(nameIndex)
                    val fileSizeFormat =
                        Formatter.formatFileSize(context, returnCursor.getLong(sizeIndex))

                    // 파일 용량 체크 1MB까지 가능
                    if (maxSize < returnCursor.getLong(sizeIndex)) {
                        Toast.makeText(
                            context,
                            R.string.qa_message_file_size_check,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    // 파일 타입 체크
                    val pattern =
                        Pattern.compile("(jpg|jpeg|png)")
                    val matcher = pattern.matcher(mimeType.lowercase(Locale.getDefault()))
                    if (matcher.matches() == false) {
                        Toast.makeText(
                            context,
                            R.string.qa_message_file_type_check,
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }

                    viewDataBinding.apply {
                        // 파일 이름
                        txtFileLabel.setVisibility(View.GONE)
                        txtFileName.setVisibility(View.VISIBLE)
                        txtFileName.setText(name)

                        // 파일 사이즈
                        val fileSize = returnCursor.getLong(sizeIndex).toFloat()
                        val temp = fileSize / maxSize
                        val progress = (temp * 100).toInt()
                        groupFileSize.setVisibility(View.VISIBLE)
                        txtCurrentSize.setText(fileSizeFormat)
                        fileProgress.setProgress(progress)
                        returnCursor.close()
                    }
                }

            }

        }


    }

    private fun btnRegisterEnable() {
        viewDataBinding.apply {
            val isRegisterAble = (btnTerms.isSelected()
                    && !TextUtils.isEmpty(editTitle.getText().toString().trim())
                    && !TextUtils.isEmpty(
                editQaContent.getText().toString().trim()
            ))
            btnRegister.setEnabled(isRegisterAble)
        }

    }

    override fun onChildResume() {
        super.onChildResume()

    }

}