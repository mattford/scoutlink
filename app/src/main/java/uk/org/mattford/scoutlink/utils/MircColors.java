package uk.org.mattford.scoutlink.utils;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for parsing and handling mIRC colors in text messages.
 *
 * @author Liato
 */
public abstract class MircColors
{
    /*
     * Colors from the "Classic" theme in mIRC.
     */
    private static final int[] colors = {
            0xFFFFFF,  // White
            0x000000,  // Black
            0x00007F,  // Blue (navy)
            0x009300,  // Green
            0xFC0000,  // Red
            0x7F0000,  // Brown (maroon)
            0x9C009C,  // Purple
            0xFC7F00,  // Orange (olive)
            0xFFFF00,  // Yellow
            0x00FC00,  // Light Green (lime)
            0x008080,  // Teal (a green/blue cyan)
            0x00FFFF,  // Light Cyan (cyan) (aqua)
            0x0000FF,  // Light Blue (royal)
            0xFF00FF,  // Pink (light purple) (fuchsia)
            0x7F7F7F,  // Grey
            0xD2D2D2   // Light Grey (silver)
    };

    private static final Pattern boldPattern = Pattern.compile("\\x02([^\\x02\\x0F]*)(\\x02|(\\x0F))?");
    private static final Pattern underlinePattern = Pattern.compile("\\x1F([^\\x1F\\x0F]*)(\\x1F|(\\x0F))?");
    private static final Pattern italicPattern = Pattern.compile("\\x1D([^\\x1D\\x0F]*)(\\x1D|(\\x0F))?");
    private static final Pattern inversePattern = Pattern.compile("\\x16([^\\x16\\x0F]*)(\\x16|(\\x0F))?");
    private static final Pattern colorPattern = Pattern.compile("\\x03(\\d{1,2})(?:,(\\d{1,2}))?([^\\x03\\x0F]*)(\\x03|\\x0F)?");
    private static final Pattern cleanupPattern = Pattern.compile("(?:\\x02|\\x1F|\\x1D|\\x0F|\\x16|\\x03(?:(?:\\d{1,2})(?:,\\d{1,2})?)?)");

    /**
     * Converts a string with mIRC style and color codes to a SpannableString with
     * all the style and color codes applied.
     *
     * @param text  A string with mIRC color codes.
     * @return      A SpannableString with all the styles applied.
     */
    public static SpannableString toSpannable(SpannableString text)
    {
        SpannableStringBuilder ssb = new SpannableStringBuilder(text);
        replaceControlCodes(boldPattern.matcher(ssb), ssb, new StyleSpan(Typeface.BOLD));
        replaceControlCodes(underlinePattern.matcher(ssb), ssb, new UnderlineSpan());
        replaceControlCodes(italicPattern.matcher(ssb), ssb, new StyleSpan(Typeface.ITALIC));

        /*
         * Inverse assumes that the background is black and the foreground is white.
         * We apply the background color first and then apply the foreground color
         * to all the parts where BackgroundColorSpans are found.
         */
        replaceControlCodes(inversePattern.matcher(ssb), ssb, new BackgroundColorSpan(colors[0] | 0xFF000000));
        BackgroundColorSpan[] inverseSpans = ssb.getSpans(0, ssb.length(), BackgroundColorSpan.class);
        for (int i = 0; i < inverseSpans.length; i++) {
            ssb.setSpan(new ForegroundColorSpan(colors[1] | 0xFF000000), ssb.getSpanStart(inverseSpans[i]),ssb.getSpanEnd(inverseSpans[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher m = colorPattern.matcher(ssb);

        while (m.find()) {
            int start = m.start();
            int end = m.end();

            Integer color = Integer.parseInt(m.group(1));
            int codelength = m.group(1).length()+1;

            if (color <= 15 && color >= 0) {
                ssb.setSpan(new ForegroundColorSpan(colors[color] | 0xFF000000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (m.group(2) != null) {
                color = Integer.parseInt(m.group(2));
                if (color <= 15 && color >= 0) {
                    ssb.setSpan(new BackgroundColorSpan(colors[color] | 0xFF000000), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                codelength = codelength + m.group(2).length() + 1;
            }

            ssb.delete(start, start+codelength);
            // Reset the matcher with the modified text so that the ending color code character can be matched again.
            m.reset(ssb);
        }
        // Remove left over codes
        return new SpannableString(removeStyleAndColors(ssb));
    }

    /**
     * Converts a string with mIRC style and color codes to a SpannableString with
     * all the style and color codes applied.
     *
     * @param text  A string with mIRC color codes.
     * @return      A SpannableString with all the styles applied.
     */
    public static SpannableString toSpannable(String text)
    {
        return toSpannable(new SpannableString(text));
    }

    /**
     * Replace the control codes
     *
     * @param m
     * @param ssb
     * @param style
     */
    private static void replaceControlCodes(Matcher m, SpannableStringBuilder ssb, CharacterStyle style)
    {
        ArrayList<Integer> toremove = new ArrayList<Integer>();

        while (m.find()) {
            toremove.add(0, m.start());
            // Remove the ending control character unless it's \x0F
            if (m.group(2) != null && m.group(2) != m.group(3)) {
                toremove.add(0, m.end()-1);
            }
            ssb.setSpan(style, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Integer i : toremove) {
            ssb.delete(i, i+1);
        }
    }

    /**
     * Removes mIRC color and style codes and returns the message without them.
     *
     * @param text  A message with mirc colors and styles.
     * @return      The same message with all the colors and styles removed.
     */
    public static String removeStyleAndColors(String text)
    {
        return cleanupPattern.matcher(text).replaceAll("");
    }

    /**
     * Removes mIRC color and style codes and returns the message without them.
     *
     * @param text  A message with mirc colors and styles.
     * @return      The same message with all the colors and styles removed.
     */
    public static SpannableStringBuilder removeStyleAndColors(SpannableStringBuilder text)
    {
        ArrayList<int[]> toremove = new ArrayList<int[]>();
        Matcher m = cleanupPattern.matcher(text);
        while (m.find()) {
            toremove.add(0, new int[] {m.start(), m.end()});
        }
        for (int[] i : toremove) {
            text.delete(i[0], i[1]);
        }
        return text;
    }

    public static int[] getColours() {
        return colors;
    }

    public static String applyControlCodes(Spannable spannableString) {
        StringBuilder sb = new StringBuilder();
        CharacterStyle[] spans = spannableString.getSpans(0, spannableString.length(), CharacterStyle.class);
        for (int i = 0; i < spannableString.length(); i++) {
            char c = spannableString.charAt(i);
            ArrayList<CharacterStyle> spansStarting = new ArrayList<>();
            ArrayList<CharacterStyle> spansEnding = new ArrayList<>();
            ArrayList<CharacterStyle> otherSpans = new ArrayList<>();
            for (CharacterStyle span : spans) {
                int spanStart = spannableString.getSpanStart(span);
                int spanEnd = spannableString.getSpanEnd(span);
                if (spanEnd == i) {
                    spansEnding.add(span);
                } else if (spanStart == i) {
                    spansStarting.add(span);
                } else {
                    otherSpans.add(span);
                }
            }
            sb.append(getEndControlCode(spansEnding, otherSpans));
            sb.append(getStartControlCode(spansStarting));
            sb.append(c);
        }
        return sb.toString();
    }

    private static String getStartControlCode(ArrayList<CharacterStyle> styles) {
        StringBuilder controlString = new StringBuilder();
        int foregroundColour = 0;
        int backgroundColour = 0;
        for (CharacterStyle style : styles) {
            if (style.getClass() == StyleSpan.class) {
                StyleSpan span = (StyleSpan) style;
                if (span.getStyle() == Typeface.BOLD || span.getStyle() == Typeface.BOLD_ITALIC) {
                    controlString.append(Character.toChars(2));
                }
                if (span.getStyle() == Typeface.ITALIC || span.getStyle() == Typeface.BOLD_ITALIC) {
                    controlString.append(Character.toChars(29));
                }
            } else if (style.getClass() == UnderlineSpan.class) {
                controlString.append(Character.toChars(31));
            } else if (style.getClass() == ForegroundColorSpan.class) {
                foregroundColour = ((ForegroundColorSpan) style).getForegroundColor();
            } else if (style.getClass() == BackgroundColorSpan.class) {
                backgroundColour = ((BackgroundColorSpan) style).getBackgroundColor();
            }
        }
        StringBuilder colourControlCode = new StringBuilder();
        if (foregroundColour != 0) {
            colourControlCode.append(Character.toChars(3));
            colourControlCode.append(getMircColor(foregroundColour));
            if (backgroundColour != 0) {
                colourControlCode.append(",");
                colourControlCode.append(getMircColor(backgroundColour));
            }
            controlString.append(colourControlCode.toString());
        } else if (backgroundColour != 0) {
            colourControlCode.append(Character.toChars(3));
            colourControlCode.append("99,");
            colourControlCode.append(getMircColor(backgroundColour));
        }
        return controlString.append(colourControlCode).toString();
    }

    private static String getEndControlCode(ArrayList<CharacterStyle> endingStyles, ArrayList<CharacterStyle> otherStyles) {
        ArrayList<Integer> otherTypefaces = new ArrayList<>();
        for (CharacterStyle otherStyle : otherStyles) {
            if (otherStyle.getClass() == StyleSpan.class) {
                StyleSpan otherSpan = (StyleSpan) otherStyle;
                otherTypefaces.add(otherSpan.getStyle());
            }
        }
        ArrayList<CharacterStyle> styleSpans = new ArrayList<>();
        StringBuilder controlCode = new StringBuilder();
        for (CharacterStyle style : endingStyles) {
            if (style.getClass() == ForegroundColorSpan.class || style.getClass() == BackgroundColorSpan.class) {
                // If a foreground colour span is ending, but there is still a background colour span (and vice versa),
                // we'll need to add another start control code here with the remaining span.
                int foregroundColour = 0;
                int backgroundColor = 0;
                for (CharacterStyle otherSpan : otherStyles) {
                    if (otherSpan.getClass() == BackgroundColorSpan.class) {
                        backgroundColor = ((BackgroundColorSpan) otherSpan).getBackgroundColor();
                    } else if (otherSpan.getClass() == ForegroundColorSpan.class) {
                        foregroundColour = ((ForegroundColorSpan) otherSpan).getForegroundColor();
                    }
                }
                controlCode.append(Character.toChars(3));
                if (foregroundColour != 0) {
                    controlCode.append(getMircColor(foregroundColour));
                } else if (backgroundColor != 0) {
                    controlCode.append("99");
                }
                if (backgroundColor != 0) {
                    controlCode.append(",");
                    controlCode.append(getMircColor(backgroundColor));
                }
            } else if (style.getClass() == StyleSpan.class) {
                StyleSpan span = (StyleSpan) style;
                if (span.getStyle() == Typeface.BOLD_ITALIC) {
                    if (otherTypefaces.contains(Typeface.ITALIC) && !otherTypefaces.contains(Typeface.BOLD)) {
                        styleSpans.add(new StyleSpan(Typeface.BOLD));
                    }
                    if (otherTypefaces.contains(Typeface.BOLD) && !otherTypefaces.contains(Typeface.ITALIC)) {
                        styleSpans.add(new StyleSpan(Typeface.ITALIC));
                    }
                } else if (!otherTypefaces.contains(span.getStyle())) {
                    styleSpans.add(style);
                }
            }
        }
        return controlCode.append(getStartControlCode(styleSpans)).toString();
    }

    private static int getMircColor(int color) {
        for (int i = 0; i < colors.length; i++) {
            if ((colors[i] | 0xFF000000) == color) {
                return i;
            }
        }
        return 99;
    }
}
