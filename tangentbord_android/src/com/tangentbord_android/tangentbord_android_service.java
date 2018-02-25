
package com.tangentbord_android;

import android.inputmethodservice.*;
import android.view.inputmethod.*;
import android.view.*;
import android.media.*;

public class tangentbord_android_service extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

	private KeyboardView kv;
	private Keyboard keyboard_normal;
	private Keyboard keyboard_shift;
	private Keyboard keyboard_alt;
	boolean shiftPressed;
	boolean altModeOn;

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		InputConnection ic = getCurrentInputConnection();
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:
		case 0x232B:
			//Backspace: delete char before
			ic.deleteSurroundingText(1, 0);
			break;
		case 0x2421:
			//DEL: delete selected text
			CharSequence selectedText = ic.getSelectedText(0);
			if (selectedText != null) {
				ic.deleteSurroundingText(0, ic.getSelectedText(0).length());
			}
			break;
		case 0x2326:
			//Forward delete: delete char after
			ic.deleteSurroundingText(0, 1);
			break;
		case 0x21EA: //CAPS LOCK
			this.keyboard_normal.setShifted(!this.keyboard_normal.isShifted());
			this.keyboard_shift.setShifted(!this.keyboard_shift.isShifted());
			this.keyboard_alt.setShifted(!this.keyboard_alt.isShifted());
			kv.invalidateAllKeys();
			break;
		case 0x2387: //"CAPS" ALT
			if (altModeOn) {
				this.altModeOn = false;
				kv.setKeyboard(this.keyboard_normal);
				kv.invalidateAllKeys();
			} else {
				this.altModeOn = true;
				kv.setKeyboard(this.keyboard_alt);
				kv.invalidateAllKeys();
			}
			break;
		case 0x2732: //CTRL
		case 0x2190: //left
		case 0x2193: //down
		case 0x2192: //right
		case 0x2191: //up
		case Keyboard.KEYCODE_SHIFT:
		case 0x21e7:
		case Keyboard.KEYCODE_DONE:
			break;
		default:
			if (kv.isShifted() && Character.isLetter(primaryCode)) {
				ic.commitText(new String(Character.toChars(primaryCode)).toUpperCase(), 1);
			} else {
				ic.commitText(String.valueOf(Character.toChars(primaryCode)), 1);
			}
			break;
		}
	}

	@Override
	public void onPress(int primaryCode) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		InputConnection ic = getCurrentInputConnection();
		switch (primaryCode) {
		case 0x0020:
			am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
			break;
		case Keyboard.KEYCODE_DONE:
			am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
			break;
		case Keyboard.KEYCODE_DELETE:
		case 0x232B:
		case 0x2421:
		case 0x2326:
			am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
			break;
		case Keyboard.KEYCODE_SHIFT:
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			this.keyboard_normal.setShifted(!this.keyboard_normal.isShifted());
			this.keyboard_shift.setShifted(!this.keyboard_shift.isShifted());
			this.keyboard_alt.setShifted(!this.keyboard_alt.isShifted());
			kv.setKeyboard(this.keyboard_shift);
			this.shiftPressed = true;
			kv.invalidateAllKeys();
			break;
		case 0x2732: //CTRL
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT));
			break;
		case 0x2190: //left
		{
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
			if (et != null && this.shiftPressed && et.selectionStart < et.selectionEnd) {
				ic.setSelection(et.selectionStart, et.selectionEnd - 1);
			} else {
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT));
			}
			break;
		}
		case 0x2193: //down
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
			break;
		case 0x2192: //right
		{
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			ExtractedText et = ic.getExtractedText(new ExtractedTextRequest(), 0);
			if (et != null && this.shiftPressed) {
				ic.setSelection(et.selectionStart, et.selectionEnd + 1);
			} else {
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT));
			}
			break;
		}
		case 0x2191: //up
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
			break;
		default:
			am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
		}
	}

	@Override
	public void onRelease(int primaryCode) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		InputConnection ic = getCurrentInputConnection();
		switch (primaryCode) {
		case Keyboard.KEYCODE_DONE:
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
			break;
		case 0x2732: //CTRL
			ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_CTRL_LEFT));
			break;
		case 0x2190: //left
			if (!this.shiftPressed)
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
			break;
		case 0x2193: //down
			if (!this.shiftPressed)
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN));
			break;
		case 0x2192: //right
			if (!this.shiftPressed)
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
			break;
		case 0x2191: //up
			if (!this.shiftPressed)
				ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP));
			break;
		default:
			if (this.shiftPressed) {
				this.shiftPressed = false;
				this.keyboard_normal.setShifted(!this.keyboard_normal.isShifted());
				this.keyboard_shift.setShifted(!this.keyboard_shift.isShifted());
				this.keyboard_alt.setShifted(!this.keyboard_alt.isShifted());
				if (this.altModeOn) {
					kv.setKeyboard(this.keyboard_alt);
				} else {
					kv.setKeyboard(this.keyboard_normal);
				}
				kv.invalidateAllKeys();
			}
			break;
		}
	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeUp() {
	}

	@Override
	public View onCreateInputView() {
		kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
		keyboard_normal = new Keyboard(this, R.xml.qwerty, R.integer.mode_normal);
		keyboard_shift = new Keyboard(this, R.xml.qwerty, R.integer.mode_shift);
		keyboard_alt = new Keyboard(this, R.xml.qwerty, R.integer.mode_alt);
		kv.setKeyboard(keyboard_normal);
		kv.setOnKeyboardActionListener(this);
		return kv;
	}

}
