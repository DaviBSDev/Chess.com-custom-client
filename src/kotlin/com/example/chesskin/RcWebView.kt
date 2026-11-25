package com.example.chesskin

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.webkit.WebView
import kotlin.math.abs

/**
 * WebView com "clique direito" por gesto:
 * segure um dedo e use o OUTRO = botão direito do mouse
 * (toque/arraste com o 2º dedo desenha setas e marca casas no tabuleiro).
 *
 * Todo toque simples de um dedo é um clique ESQUERDO normal. O toque do 1º dedo só é
 * enviado à página quando ele se move (arrastar) ou é solto (clique); se um 2º dedo
 * chegar antes, o 1º nunca chega à página, então segurar não apaga as setas.
 */
class RcWebView(context: Context) : WebView(context) {
    private var rc = false          // 2º dedo em modo botão direito
    private var rcId = -1           // id do ponteiro do 2º dedo
    private var ignoreRest = false  // ignora o resto do gesto (até soltar tudo)
    private var pending: MotionEvent? = null  // DOWN do 1º dedo ainda não enviado
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                clearPending()
                rc = false
                ignoreRest = false
                rcId = -1
                pending = MotionEvent.obtain(e)
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> if (!rc && e.pointerCount == 2) {
                if (!ignoreRest) {
                    if (pending != null) {
                        clearPending() // o 1º dedo nunca chegou à página
                    } else {
                        // o 1º dedo já estava na página: cancela o toque dele
                        val c = MotionEvent.obtain(e)
                        c.action = MotionEvent.ACTION_CANCEL
                        super.onTouchEvent(c)
                        c.recycle()
                    }
                }
                rc = true
                ignoreRest = true
                rcId = e.getPointerId(e.actionIndex)
                rcEvent(e, e.actionIndex, "down")
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (rc) {
                    val i = e.findPointerIndex(rcId)
                    if (i >= 0) rcEvent(e, i, "move")
                    return true
                }
                val p = pending
                if (p != null) {
                    if (abs(e.x - p.x) > slop || abs(e.y - p.y) > slop) flushPending()
                    else return true
                }
            }

            MotionEvent.ACTION_POINTER_UP -> if (ignoreRest) {
                if (rc && e.getPointerId(e.actionIndex) == rcId) {
                    rcEvent(e, e.actionIndex, "up")
                    rc = false
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (ignoreRest) {
                    if (rc) {
                        val i = e.findPointerIndex(rcId).let { if (it >= 0) it else 0 }
                        rcEvent(e, i, "up")
                    }
                    rc = false
                    ignoreRest = false
                    rcId = -1
                    return true
                }
                if (pending != null) {
                    if (e.actionMasked == MotionEvent.ACTION_UP) {
                        flushPending() // clique simples: envia DOWN e depois o UP
                    } else {
                        clearPending()
                        return true
                    }
                }
            }
        }
        if (ignoreRest) return true
        return super.onTouchEvent(e)
    }

    private fun flushPending() {
        val p = pending ?: return
        pending = null
        super.onTouchEvent(p)
        p.recycle()
    }

    private fun clearPending() {
        pending?.recycle()
        pending = null
    }

    /** Dispara na página um evento de mouse (botão direito) na posição do ponteiro [idx]. */
    private fun rcEvent(src: MotionEvent, idx: Int, type: String) {
        val x = src.getX(idx)
        val y = src.getY(idx)
        evaluateJavascript("(" + RC_JS + ")('" + type + "'," + x + "," + y + ")", null)
    }
}

// x e y chegam em pixels da tela; a página usa pixels CSS (divide por devicePixelRatio)
private val RC_JS = """
function(t, x, y) {
  var d = window.devicePixelRatio || 1, cx = x / d, cy = y / d;
  var el = document.elementFromPoint(cx, cy) || document.body;
  function fire(type, isPtr, btns) {
    var C = isPtr ? (window.PointerEvent || MouseEvent) : MouseEvent;
    var o = {bubbles: true, cancelable: true, composed: true, view: window,
             clientX: cx, clientY: cy, screenX: cx, screenY: cy,
             button: 2, buttons: btns, detail: 1};
    if (isPtr) { o.pointerId = 1; o.pointerType = 'mouse'; o.isPrimary = true; }
    el.dispatchEvent(new C(type, o));
  }
  if (t === 'down') { fire('pointerdown', true, 2); fire('mousedown', false, 2); }
  else if (t === 'move') { fire('pointermove', true, 2); fire('mousemove', false, 2); }
  else { fire('pointerup', true, 0); fire('mouseup', false, 0); fire('contextmenu', false, 0); }
}
"""
