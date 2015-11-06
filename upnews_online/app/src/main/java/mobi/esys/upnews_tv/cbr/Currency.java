package mobi.esys.upnews_tv.cbr;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="Valute")
public class Currency {
    @Element(name="CharCode")
    private String currCharCode;
    @Element(name="Value")
    private String currValue;
    @Element (name="Nominal")
    private String nominal;

    public final String getCurrCharCode() {
        return currCharCode;
    }



    public final String getCurrValue() {
        return currValue;
    }


    public final String getNominal() {
        return this.nominal;
    }

    @Override
    public final String toString() {
        return "Currency{" + "currCharCode='" + currCharCode + '\'' + ", currValue='" + currValue + '\'' + '}';
    }
}
