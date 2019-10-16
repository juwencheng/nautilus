package com.jarvanmo.nautilus


import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
//        val debuggable = call.argument("debuggable") ?: false
        val debuggable = true
        if (debuggable) {
            AlibcTradeCommon.turnOnDebug()
            AlibcTradeBiz.turnOnDebug()
            MemberSDK.turnOnDebug()
        } else {
            AlibcTradeCommon.turnOffDebug()
            AlibcTradeBiz.turnOffDebug()
            MemberSDK.turnOnDebug()
        }
        Log.i("蒲说", "调用异步初始化")
        //电商SDK初始化
        AlibcTradeSDK.asyncInit(registry.activity().application, object : AlibcTradeInitCallback {
            override fun onSuccess() {
                Log.i("蒲说", "异步初始化成功")
                result.success(mapOf(
                        keyPlatform to keyAndroid,
                        keyResult to true
                ))
//                result(@{nautilusKeyPlatform: nautilusKeyIOS, nautilusKeyResult: @NO, nautilusKeyErrorCode: @(error.code),nautilusKeyErrorMessage: error.description});
            }

            override fun onFailure(code: Int, msg: String) {
                Log.i("蒲说", "异步初始化失败 $code $msg")
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

    fun openUrl(call: MethodCall, result: MethodChannel.Result) {
        val pageUrl = call.argument<String?>("pageUrl") as String
        openByUrl(pageUrl, call, result)
    }

    fun openAuthUrl(call: MethodCall, result: MethodChannel.Result) {
        val pageUrl = call.argument<String?>("pageUrl") as String
        Log.i("蒲说", "openAuthUrl 拦截地址  $pageUrl ")
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

        // TODO: 如果下面代码还是不行，就只能实现webViewClient的中拦截链接跳转的代码了
        // 代码可参考蒲说工程 AuthWebViewActivity.java 第77行
        AlibcTrade.openByUrl(registry.activity(), "", pageUrl, null, object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Log.i("蒲说", "openAuthUrl -150- 拦截地址  $url ")
                if (url.indexOf("http://app.pslife.com.cn/api/tbk/specialAuthv1") == 0) {
                    result.success(mapOf(
                            "data" to url,
                            "result" to 1
                    ))
//                    val intent = getIntent()
//                    val result = HashMap<String, Any>()
//                    result.put("data", url)
//                    result.put("result", 1)
//                    intent.putExtra("result", result as Serializable)
//                    setResult(RESULT_OK, intent)
//                    finish()
                    return true
                } else if (url.indexOf("http://app.pslife.com.cn/api/tbk/relationAuthv1") == 0) {
                    result.success(mapOf(
                            "data" to url,
                            "result" to 1
                    ))
//                    val intent = getIntent()
//                    val result = HashMap<String, Any>()
//                    result.put("data", url)
//                    result.put("result", 1)
//                    intent.putExtra("result", result as Serializable)
//                    setResult(RESULT_OK, intent)
//                    finish()
                    return true
                }
                view.loadUrl(url)
                return false
            }
        }, WebChromeClient(), buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback)
//        openResultCode = AlibcTrade.show(registry.activity(), page, buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback)

    }

    fun openMyCart(call: MethodCall, result: MethodChannel.Result) {
        openByPage(AlibcMyCartsPage(), call, result)
    }

    fun openOrderList(call: MethodCall, result: MethodChannel.Result) {
        openByPage(AlibcMyOrdersPage(0, true), call, result)
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
        AlibcTrade.openByBizCode(registry.activity(), page, null, null, null, "detail", buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback);
    }

    private fun openByUrl(url: String, call: MethodCall, result: MethodChannel.Result) {
        Log.i("蒲说", "openByUrl -253-    拦截地址  $url ")
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

        // TODO: 如果下面代码还是不行，就只能实现webViewClient的中拦截链接跳转的代码了
        // 代码可参考蒲说工程 AuthWebViewActivity.java 第77行
//        openByUrl(registry.activity(), "", url, null, WebViewClient(), WebChromeClient(), buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback);
        AlibcTrade.openByUrl(registry.activity(), "", url, null, WebViewClient(), WebChromeClient(), buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback);
//        openResultCode = AlibcTrade.show(registry.activity(), page, buildShowParams(call), buildTaoKeParams(call), call.argument<Map<String, String>?>("extParams"), tradeCallback)

    }


    private fun buildTaoKeParams(call: MethodCall): AlibcTaokeParams? {
        var taoKe: AlibcTaokeParams? = null
        val taoKeParams = call.argument<Map<String, Any?>?>("taoKeParams")
        if (taoKeParams != null) {
            taoKe = AlibcTaokeParams(taoKeParams["taoKeParamsPid"]?.toString(), taoKeParams["taoKeParamsUnionId"]?.toString(), taoKeParams["taoKeParamsSubPid"]?.toString())
//            taoKe.pid = taoKeParams["taoKeParamsPid"]?.toString()
//            taoKe.subPid = taoKeParams["taoKeParamsSubPid"]?.toString()
//            taoKe.unionId = taoKeParams["taoKeParamsUnionId"]?.toString()
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
        val alibcShowParams = AlibcShowParams(intToOpenType(openType))
        alibcShowParams.backUrl = call.argument<String?>("backUrl")
        alibcShowParams.clientType = call.argument("schemeType") ?: "tmall_scheme"
        val openFailedMode = call.argument("openNativeFailedMode") ?: 0
        alibcShowParams.nativeOpenFailedMode = intToFailedMode(openFailedMode)
        return alibcShowParams
    }

    private fun intToOpenType(type: Int): OpenType = when (type) {
        1 -> OpenType.Native
        else -> OpenType.Auto
    }

    private fun intToFailedMode(type: Int) = when (type) {
        1 -> AlibcFailModeType.AlibcNativeFailModeJumpDOWNLOAD
        2 -> AlibcFailModeType.AlibcNativeFailModeJumpBROWER
        3 -> AlibcFailModeType.AlibcNativeFailModeNONE
        else -> AlibcFailModeType.AlibcNativeFailModeNONE
    }
}







