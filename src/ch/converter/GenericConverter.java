package ch.converter;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import ch.test.entities.ExchangeRatePair;
import ch.test.interfaces.IConvertible;

/**
 * Generic converter for Comparisn of ExchangeRate Pair entities to be used at
 * client side (faces)
 * 
 * @author Marc Dünki
 */
@FacesConverter(forClass = ch.test.entities.ExchangeRatePair.class, value = "generic")
public class GenericConverter implements Converter {

	@SuppressWarnings("unchecked")
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		ExchangeRatePair ret = null;
		UIComponent src = arg1;
		if (src != null) {
			List<UIComponent> childs = src.getChildren();
			UISelectItems itens = null;
			if (childs != null) {
				for (UIComponent ui : childs) {
					if (ui instanceof UISelectItems) {
						itens = (UISelectItems) ui;
						break;
					} else if (ui instanceof UISelectItem) {
						UISelectItem item = (UISelectItem) ui;
						try {
							IConvertible val = (IConvertible) item
									.getItemValue();
							if (arg2.equals("" + val.getId())) {
								ret = (ExchangeRatePair) val;
								break;
							}
						} catch (Exception e) {
						}
					}
				}
			}

			if (itens != null) {
				List<IConvertible> values = (List<IConvertible>) itens
						.getValue();
				if (values != null) {
					for (IConvertible val : values) {
						if (arg2.equals("" + val.getId())) {
							ret = (ExchangeRatePair) val;
							break;
						}
					}
				}
			}
		}
		return ret;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String ret = "";
		if (arg2 != null && arg2 instanceof ExchangeRatePair) {
			ExchangeRatePair m = (ExchangeRatePair) arg2;
			if (m != null) {
				Integer id = m.getId();
				if (id != null) {
					ret = id.toString();
				}
			}
		}
		return ret;
	}

}
