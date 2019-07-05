package com.jarvanmo.nautilus

import android.content.Intent
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.ali.auth.third.core.MemberSDK
import com.alibaba.baichuan.android.trade.AlibcTrade
import com.alibaba.baichuan.android.trade.AlibcTradeSDK
import com.alibaba.baichuan.android.trade.callback.AlibcTradeCallback
import com.alibaba.baichuan.android.trade.callback.AlibcTradeInitCallback
import com.alibaba.baichuan.android.trade.model.AlibcShowParams
import com.alibaba.baichuan.android.trade.model.OpenType
import com.alibaba.baichuan.android.trade.page.*
import com.alibaba.baichuan.trade.biz.AlibcTradeBiz
import com.alibaba.baichuan.trade.biz.applink.adapter.AlibcFailModeType
import com.alibaba.baichuan.trade.biz.context.AlibcResultType
import com.alibaba.baichuan.trade.biz.context.AlibcTradeResult
import com.alibaba.baichuan.trade.biz.core.taoke.AlibcTaokeParams
import com.alibaba.baichuan.trade.common.AlibcTradeCommon
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

/***
 * Created by mo on 2018/11/23
 * 冷风如刀，以大地为砧板，视众生为鱼肉。`
 * 万里飞雪，将穹苍作烘炉，熔万物为白银。
 **/
internal class TradeHandler(private val registry: PluginRegistry.Registrar) {
    fun initTradeAsync(call: MethodCall, result: MethodChannel.Result) {
        val version = call.argument<String?>("version")
        if (!version.isNullOrBlank()) {
            AlibcTradeSDK.setISVVersion(version)
        }
        val debuggable = call.argument("debuggable") ?: false
        if (debuggable) {
            AlibcTradeCommon.turnOnDebug()
            AlibcTradeBiz.turnOnDebug()
            MemberSDK.turnOnDebug()
        } else {
            AlibcTradeCommon.turnOffDebug()
            AlibcTradeBiz.turnOffDebug()
            MemberSDK.turnOnDebug()
        }

        //电商SDK初始化
        AlibcTradeSDK.asyncInit(registry.activity().application, object : AlibcTradeInitCallback {
            override fun onSuccess() {
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to true
                ))
//                result(@{nautilusKeyPlatform: nautilusKeyIOS, nautilusKeyResult: @NO, nautilusKeyErrorCode: @(error.code),nautilusKeyErrorMessage: error.description});
            }

            override fun onFailure(code: Int, msg: String) {
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to code,
                        keyErrorMessage to msg
                ))
            }
        })

    }

    fun destroy() {
        AlibcTradeSDK.destory()
    }


    fun openItemDetail(call: MethodCall, result: MethodChannel.Result) {
        val itemID = call.argument<String?>("itemID")
        openByPage(AlibcDetailPage(itemID), call, result)
    }

    fun openUrl(call: MethodCall, result: MethodChannel.Result){
        val pageUrl = call.argument<String?>("pageUrl")
        openByPage(AlibcPage(pageUrl), call, result)
    }
    fun openMyCart(call: MethodCall, result: MethodChannel.Result) {
        openByPage(AlibcMyCartsPage(), call, result)
    }
    fun openOrderList(call: MethodCall, result: MethodChannel.Result) {
        openByPage(AlibcMyOrdersPage(0,true), call, result)
    }
    fun openAuthUrl(call: MethodCall, result: MethodChannel.Result) {
        val pageUrl = call.argument<String?>("pageUrl")
        openAuthPage(AlibcPage(pageUrl), call, result)
    }
    private fun openAuthPage(page: AlibcBasePage, call: MethodCall, result: MethodChannel.Result) {
        var openResultCode = -1
        val tradeCallback = object : AlibcTradeCallback {
            override fun onTradeSuccess(tradeResult: AlibcTradeResult?) {
                if (tradeResult == null) {
                    result.success(mapOf(
                            "openResultCode" to openResultCode,
                            keyPlatform to keyAndroid,
                            keyResult to false,
                            keyErrorCode to -99999,
                            keyErrorMessage to "tradeResult is null"
                    ))
                    return
                }
                when {
                    tradeResult.resultType == AlibcResultType.TYPEPAY -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to 0,
                                "paySuccessOrders" to tradeResult.payResult.payFailedOrders,
                                "payFailedOrders" to tradeResult.payResult.payFailedOrders
                        ))
                    }
                    tradeResult.resultType == AlibcResultType.TYPECART -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to 1
                        ))
                    }
                    else -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to -1
                        ))
                    }
                }

            }

            override fun onFailure(code: Int, message: String?) {
                result.success(mapOf(
                        "openResultCode" to openResultCode,
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to code,
                        keyErrorMessage to message
                ))
            }
        }
        val webClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("debug", url)
                url?.let { realUrl ->
                    if (realUrl.indexOf("http://app.pslife.com.cn/api/tbk/specialAuthv1") == 0){
                        result.success(mapOf("success" to 1, "data" to realUrl))
                        return true
                    }
                }

                return false
            }
        }

//        val webView =  WebView(registry.context())
        val webActivity = WebActivity()
        val code = AlibcTrade.show(webActivity, webActivity.webView, webClient, WebChromeClient(), page, buildShowParams(call),buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback);
        Log.d("debug", code.toString())
//        if (code == 1) {
//            val intent = Intent()
//            //获取intent对象
//            intent.setClass(registry.activity(),::class.java)
//            // 获取class是使用::反射(那么问题来了,反射是个什么鬼?👻👻👻👻小白的悲哀啊,赶紧研究研究去)
//            startActivity(intent)
//        }
//        openResultCode = AlibcTrade.show(registry.activity(), page, buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback)
    }

    private fun openByPage(page: AlibcBasePage, call: MethodCall, result: MethodChannel.Result) {
        var openResultCode = -1
        val tradeCallback = object : AlibcTradeCallback {
            override fun onTradeSuccess(tradeResult: AlibcTradeResult?) {
                if (tradeResult == null) {
                    result.success(mapOf(
                            "openResultCode" to openResultCode,
                            keyPlatform to keyAndroid,
                            keyResult to false,
                            keyErrorCode to -99999,
                            keyErrorMessage to "tradeResult is null"
                    ))
                    return
                }
                when {
                    tradeResult.resultType == AlibcResultType.TYPEPAY -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to 0,
                                "paySuccessOrders" to tradeResult.payResult.payFailedOrders,
                                "payFailedOrders" to tradeResult.payResult.payFailedOrders
                        ))
                    }
                    tradeResult.resultType == AlibcResultType.TYPECART -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to 1
                        ))
                    }
                    else -> {
                        result.success(mapOf(
                                "openResultCode" to openResultCode,
                                keyPlatform to keyAndroid,
                                keyResult to true,
                                "tradeResultType" to -1
                        ))
                    }
                }

            }

            override fun onFailure(code: Int, message: String?) {
                result.success(mapOf(
                        "openResultCode" to openResultCode,
                        keyPlatform to keyAndroid,
                        keyResult to false,
                        keyErrorCode to code,
                        keyErrorMessage to message
                ))
            }
        }
        openResultCode = AlibcTrade.show(registry.activity(), page, buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback)

    }


    private fun buildTaoKeParams(call: MethodCall): AlibcTaokeParams? {
        var taoKe: AlibcTaokeParams? = null
        val taoKeParams = call.argument<Map<String, Any?>?>("taoKeParams");
        if (taoKeParams != null) {
            taoKe = AlibcTaokeParams()
            taoKe.pid = taoKeParams["taoKeParamsPid"]?.toString()
            taoKe.subPid = taoKeParams["taoKeParamsSubPid"]?.toString()
            taoKe.unionId = taoKeParams["taoKeParamsUnionId"]?.toString()
            taoKe.adzoneid = taoKeParams["taoKeParamsAdzoneId"]?.toString()
            taoKe.extraParams = if (taoKeParams["taoKeParamsExtParams"] != null) {
                taoKeParams["taoKeParamsExtParams"] as HashMap<String, String>
            } else {
                emptyMap()
            }
        }

        return taoKe
    }

    private fun buildShowParams(call: MethodCall): AlibcShowParams {
        val openType = call.argument("openType") ?: 0
        val alibcShowParams = AlibcShowParams(intToOpenType(openType), false)
        alibcShowParams.backUrl = call.argument<String?>("backUrl")
        alibcShowParams.clientType = call.argument("schemeType") ?: "tmall_scheme"
        val openFailedMode = call.argument("openNativeFailedMode") ?: 0
        alibcShowParams.nativeOpenFailedMode = intToFailedMode(openFailedMode)
        return alibcShowParams
    }

    private fun intToOpenType(type: Int): OpenType = when (type) {
        1 -> OpenType.Native
        2 -> OpenType.H5
        else -> OpenType.Auto
    }

    private fun intToFailedMode(type: Int) = when (type) {
        1 -> AlibcFailModeType.AlibcNativeFailModeJumpDOWNLOAD
        2 -> AlibcFailModeType.AlibcNativeFailModeJumpBROWER
        3 -> AlibcFailModeType.AlibcNativeFailModeNONE
        else -> AlibcFailModeType.AlibcNativeFailModeNONE
    }
}







