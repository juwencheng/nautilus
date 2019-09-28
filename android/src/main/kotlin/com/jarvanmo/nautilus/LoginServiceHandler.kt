package com.jarvanmo.nautilus


import android.util.Log
import com.ali.auth.third.ui.context.CallbackContext
import com.alibaba.baichuan.trade.biz.login.AlibcLogin
import com.alibaba.baichuan.trade.biz.login.AlibcLoginCallback
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

/***
 * Created by mo on 2018/11/22
 * 冷风如刀，以大地为砧板，视众生为鱼肉。
 * 万里飞雪，将穹苍作烘炉，熔万物为白银。
 **/
internal class LoginServiceHandler(private val registrar: PluginRegistry.Registrar) {

    fun initLogin( result: MethodChannel.Result){
        Log.i("蒲说", "初始化登录")
       AlibcLogin.getInstance().init(object :AlibcLoginCallback{
            override fun onSuccess(code: Int, msg1: String, msg2: String) {
                Log.i("蒲说", "初始化登录成功 " + code + "code")
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to true,
                        "code" to code
                ))
            }



            override fun onFailure(p0: Int, p1: String?) {
                Log.i("蒲说", "初始化登录失败 $p0 msg $p1")
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to p0,
                        keyErrorMessage to p1
                ))
            }

        })
    }

    fun handleLogin(call: MethodCall, result: MethodChannel.Result) {
        registrar.addActivityResultListener { requestCode, resultCode, data ->
            CallbackContext.onActivityResult(requestCode, resultCode, data)
        }

        val alibcLogin = AlibcLogin.getInstance()
        Log.i("蒲说", "开始登录 ")
        alibcLogin.showLogin(object : AlibcLoginCallback {
            override fun onSuccess(p0: Int, msg1: String, msg2: String) {
                Log.i("蒲说", "登录成功 $p0 msg $msg1 msg2 $msg2 ")
                val user = mapOf(
                        "avatarUrl" to alibcLogin.session.avatarUrl,
                        "nick" to alibcLogin.session.nick,
                        "openId" to alibcLogin.session.openId,
                        "openSid" to alibcLogin.session.openSid,
                        "userId" to alibcLogin.session.userid,
                        "topAccessToken" to alibcLogin.session.topAccessToken,
                        "topAuthCode" to alibcLogin.session.topAuthCode,
                        "topExpireTime" to alibcLogin.session.topExpireTime
                )
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to true,
                        "user" to user
                ))
            }


            override fun onFailure(errorCode: Int, errorMessage: String?) {
                Log.i("蒲说", "登录失败 $errorCode $errorMessage")
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to errorCode,
                        keyErrorMessage to errorMessage

                ))
            }
        })

    }

    fun handleIsLogin(result: MethodChannel.Result) {
        result.success(AlibcLogin.getInstance().isLogin)
    }


    fun handleGetUser(result: MethodChannel.Result) {
        var user: Map<String, String>? = null
        val alibcLogin = AlibcLogin.getInstance()
        if (alibcLogin.isLogin) {
            user = mapOf(
                    "avatarUrl" to alibcLogin.session.avatarUrl,
                    "nick" to alibcLogin.session.nick,
                    "openId" to alibcLogin.session.openId,
                    "openSid" to alibcLogin.session.openSid,
                    "userId" to alibcLogin.session.userid,
                    "topAccessToken" to alibcLogin.session.topAccessToken,
                    "topAuthCode" to alibcLogin.session.topAuthCode,
                    "topExpireTime" to alibcLogin.session.topExpireTime
            )
        }
        result.success(mapOf(
                keyPlatform to keyAndroid,
                keyResult to alibcLogin.isLogin,
                "user" to user
        ))
    }

    fun handleLogout(result: MethodChannel.Result){
        registrar.addActivityResultListener { requestCode, resultCode, data ->
            CallbackContext.onActivityResult(requestCode, resultCode, data)
        }
        val alibcLogin = AlibcLogin.getInstance()
        alibcLogin.logout(object : AlibcLoginCallback {
            override fun onSuccess(i: Int, msg1: String, msg2: String) {
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to true
                ))
            }

            override fun onFailure(errorCode: Int, errorMessage: String) {
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to errorCode,
                        keyErrorMessage to errorMessage

                ))
            }
        })


    }
}