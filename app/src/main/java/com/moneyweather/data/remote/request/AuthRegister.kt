package com.aceenter.kmgm.app.data.request

class AuthRegister(builder: Builder) {
    val userEmail: String
    val userName: String
    val nickName: String
    val phone: String
    val snsId: String
    val snsType: Int
    val tokenId: String
    val birthDay: String
    val address: String
    val avatar: String
    val confirmCode: String
    val sex: Int
    val mobileCertificationId : String
    val password: String
    val passwordConfirm: String

    init {
        userEmail = builder.userEmail
        userName = builder.userName
        nickName = builder.nickName
        phone = builder.phone
        snsId = builder.snsId
        tokenId = builder.tokenId
        birthDay = builder.birthDay
        address = builder.address
        sex = builder.sex
        snsType = builder.snsType
        avatar = builder.avatar
        confirmCode = builder.confirmCode
        mobileCertificationId = builder.mobileCertificationId
        password = builder.password
        passwordConfirm = builder.passwordConfirm

    }

    class Builder {
        var userEmail = ""
        var userName = ""
        var nickName = ""
        var phone = ""
        var snsId = ""
        var birthDay = ""
        var sex = -1
        var address = ""
        var snsType = -1
        var tokenId = ""
        var avatar = ""
        var confirmCode = ""
        var mobileCertificationId = ""
        var password = ""
        var passwordConfirm = ""

        fun userEmail(userEmail: String): Builder {
            this.userEmail = userEmail
            return this
        }

        fun userName(userName: String): Builder {
            this.userName = userName
            return this
        }

        fun nickName(nickName: String): Builder {
            this.nickName = nickName
            return this
        }

        fun phone(phone: String): Builder {
            this.phone = phone
            return this
        }

        fun snsId(snsId: String): Builder {
            this.snsId = snsId
            return this
        }

        fun birthDay(birthDay: String): Builder {
            this.birthDay = birthDay
            return this
        }

        fun sex(sex: Int): Builder {
            this.sex = sex
            return this
        }

        fun address(address: String): Builder {
            this.address = address
            return this
        }

        fun snsType(snsType: Int): Builder {
            this.snsType = snsType
            return this
        }

        fun avatar(avatar: String): Builder {
            this.avatar = avatar
            return this
        }

        fun confirmCode(confirmCode : String) : Builder{
            this.confirmCode = confirmCode
            return this
        }

        fun certificationId(cId : String) : Builder{
            this.mobileCertificationId = cId
            return this
        }

        fun password(pw : String) : Builder{
            this.password = pw
            return this
        }

        fun passwordConfirm(pw : String) : Builder{
            this.passwordConfirm = pw
            return this
        }

        fun tokenId(token : String) : Builder{
            this.tokenId = token
            return this
        }

        fun build(): AuthRegister {
            return AuthRegister(this)
        }
    }


}