//package com.example.cmpickle.basicsms;
//
//import android.view.View;
//import android.widget.EditText;
//
//import java.util.Random;
//
//public class Encryption {
//
//    View view;
//
//    public Encryption(View v) {
//        view = v;
//    }
//    /**
//     * Retrieves the string inputted into the encode text box and returns the encoded version
//     * @param view
//     */
//    public void encodeMessage(View view)
//    {
//        //Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editTextSMS);
//        String message = editText.getText().toString();
//        message = encode(message);
//        // intent.putExtra(EXTRA_MESSAGE, message);
//        // startActivity(intent);
//        editText.setText(message);
//    }
//
//    /**
//     * Retrieves the string inputted into the decode text box and returns the decoded version
//     * @param view
//     */
//    public void decodeMessage(View view)
//    {
//        EditText editText = (EditText) findViewById(R.id.editTextSMS);
//        String message = editText.getText().toString();
//        message = decode(message);
//        editText.setText(message);
//    }
//
//    /**
//     * Encodes a string.
//     *
//     * @param input
//     *            --The string to be encoded
//     * @return --Encoded version of the string
//     */
//    private static String encode(String input)
//    {
//        Random rand = new Random(42);
//        char[] letters = new char[input.length()];
//        String result = "";
//
//        // Copies over the characters of the string input to the char[] letters
//
//        for(int i = 0; i < input.length(); i++)
//            letters[i] = input.charAt(i);
//
//        // modifies the Char[]
//
//        for(int j = 0; j < input.length(); j++)
//        {
//            // modifies the ASCII value of each char by adding 47 then
//            // multiplies it by two
//
//            letters[j] = (char) ((letters[j] + 47) * 2);
//            if(j % 3 == 0 || j % 3 == 2)
//            {
//                letters[j] += 1;
//                letters[j] *= 2;
//            }
//            letters[j] += rand.nextInt(100);
//        }
//
//        // Appends the chars from letters to the end of the blank string result
//        // to achieve the encoded string
//
//        for (int k = 0; k < input.length(); k++)
//            result += letters[k];
//
//        // returns the encoded string
//
//        return result;
//    }
//
//    /**
//     * Decodes a string encoded by @encode
//     *
//     * @param input
//     *            --The string to be decoded
//     * @return --The decoded string
//     */
//    public static String decode(String input)
//    {
//        Random rand = new Random(42);
//        char[] letters = new char[input.length()];
//        String result = "";
//
//        for (int i = 0; i < input.length(); i++)
//            letters[i] = input.charAt(i);
//
//        // modifies the Char[]
//
//        for (int j = 0; j < input.length(); j++)
//        {
//            letters[j] -= rand.nextInt(100);
//            // Appends the chars from letters to the end of the blank string result
//            // to achieve the encoded string
//            if (j % 3 == 0 || j % 3 == 2)
//            {
//                letters[j] /= 2;
//                letters[j] -= 1;
//            }
//            // modifies the ASCII value of each char by adding 47 then
//            // multiplies it by two
//            letters[j] = (char) ((letters[j] / 2) - 47);
//        }
//
//        for (int k = 0; k < input.length(); k++)
//            result += letters[k];
//
//        // returns the encoded string
//
//        return result;
//    }
//
//    public void clear(View view)
//    {
//        EditText et = (EditText) findViewById(R.id.editTextSMS);
//        et.setText("");
//    }
//}
