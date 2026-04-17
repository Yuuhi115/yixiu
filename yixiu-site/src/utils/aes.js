// src/utils/aes.js
import CryptoJS from "crypto-js";

const KEY = CryptoJS.enc.Utf8.parse(import.meta.env.VITE_AES_KEY);
const IV  = CryptoJS.enc.Utf8.parse(import.meta.env.VITE_AES_IV);

export function encrypt(text) {
    const encrypted = CryptoJS.AES.encrypt(
        text,
        KEY,
        {
            iv: IV,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        }
    );
    return encrypted.toString();
}

export function decrypt(ciphertext) {
    const decrypted = CryptoJS.AES.decrypt(
        ciphertext,
        KEY,
        {
            iv: IV,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        }
    );

    return decrypted.toString(CryptoJS.enc.Utf8);
}