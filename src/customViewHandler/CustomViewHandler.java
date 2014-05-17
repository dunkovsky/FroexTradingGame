package customViewHandler;

import javax.faces.application.ViewHandler;

/**
 * 
 * ViewHandler for the client side to use in case a mobile device uses the application.
 * 
 */
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;

public class CustomViewHandler extends ViewHandlerWrapper {

	private ViewHandler wrapped;

	public CustomViewHandler(ViewHandler wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public ViewHandler getWrapped() {
		return this.wrapped;
	}

	public String calculatedRenderKitId(FacesContext context) {
		String path = FacesContext.getCurrentInstance().getExternalContext()
				.getRequestPathInfo();
		if (path.equals("/index.xhtml")) {
			return "PRIMEFACES_MOBILE";
		}
		return this.wrapped.calculateRenderKitId(context);
	}

}
