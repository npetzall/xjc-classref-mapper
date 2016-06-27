package npetzall.xjc.plugin;

import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.CClassRef;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIClass;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIDeclaration;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.impl.ElementDecl;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassRefToElementInfoMapper extends Plugin {

    private Set<String> toRemove = new HashSet<>();

    @Override
    public String getOptionName() {
        return "classref";
    }

    @Override
    public String getUsage() {
        return "Enable with -classref , will classrefs to model";
    }

    @Override
    public boolean run(Outline outline, Options opt, ErrorHandler errorHandler) throws SAXException {
        removePackageContext(outline.getAllPackageContexts().iterator());
        removePackageFromCodeModel(outline.getCodeModel().packages());
        return true;
    }

    private void removePackageFromCodeModel(Iterator<JPackage> packagesIterator) {
        while(packagesIterator.hasNext()) {
            if(toRemove.contains(packagesIterator.next().name())) {
                packagesIterator.remove();
            }
        }
    }

    private void removePackageContext(Iterator<? extends PackageOutline> packageOutlineIterator) {
        while (packageOutlineIterator.hasNext()) {
            PackageOutline packageOutline = packageOutlineIterator.next();
            if (toRemove.contains(packageOutline._package().name())) {
                packageOutlineIterator.remove();
            }
        }
    }

    @Override
    public void postProcessModel(Model model, ErrorHandler errorHandler) {
        super.postProcessModel(model, errorHandler);
        Iterator<XSElementDecl> xsElementDeclIterator = model.schemaComponent.iterateElementDecls();
        while(xsElementDeclIterator.hasNext()) {
            ElementDecl elementDecl = getElementDecl(xsElementDeclIterator.next());
            BIClass biClass = getBIClass(elementDecl);
            processClassRef(model,elementDecl,biClass);
        }
    }

    private ElementDecl getElementDecl(XSElementDecl xsElementDecl) {
        ElementDecl elementDecl = null;
        if (xsElementDecl instanceof ElementDecl) {
            elementDecl = (ElementDecl) xsElementDecl;
        }
        return elementDecl;
    }

    private BIClass getBIClass(ElementDecl elementDecl) {
        BIClass biClass = null;
        if(hasBIClass(elementDecl)) {
            BindInfo bindInfo = (BindInfo) elementDecl.getAnnotation().getAnnotation();
            for(BIDeclaration biDeclaration: bindInfo.getDecls()) {
                if (biDeclaration instanceof BIClass) {
                    biClass = (BIClass) biDeclaration;
                }
            }
        }
        return biClass;
    }

    private boolean hasBIClass(ElementDecl elementDecl) {
        return elementDecl != null &&
                elementDecl.getAnnotation() != null &&
                elementDecl.getAnnotation().getAnnotation() != null &&
                elementDecl.getAnnotation().getAnnotation() instanceof BindInfo;
    }

    private void processClassRef(Model model, ElementDecl elementDecl, BIClass biClass) {
        if (shouldProcessClassRef(model, elementDecl, biClass)) {
            new CElementInfo(
                    model,
                    new QName(elementDecl.getTargetNamespace(), elementDecl.getName()),
                    new CClassInfoParent.Package(model.codeModel._package(biClass.getExistingClassRef().substring(0, biClass.getExistingClassRef().lastIndexOf('.')))),
                    new CClassRef(model, elementDecl, biClass, null),
                    elementDecl.getDefaultValue(),
                    elementDecl,
                    null,
                    elementDecl.getLocator()
            );
            toRemove.add(biClass.getExistingClassRef().substring(0, biClass.getExistingClassRef().lastIndexOf('.')));
        }
    }

    private boolean shouldProcessClassRef(Model model, ElementDecl elementDecl, BIClass biClass) {
        return model != null && elementDecl != null && biClass != null && elementDecl.isGlobal() && biClass.getExistingClassRef() != null;
    }
}
