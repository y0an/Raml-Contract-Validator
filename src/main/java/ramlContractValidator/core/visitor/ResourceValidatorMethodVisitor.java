package ramlContractValidator.core.visitor;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.expr.AnnotationExpr;
import org.apache.maven.plugin.logging.Log;
import org.raml.model.ActionType;
import org.raml.model.Raml;
import org.raml.model.parameter.QueryParameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by shendrickson1 on 6/23/14.
 *
 * @author Scott Hendrickson
 *
 */
public class ResourceValidatorMethodVisitor extends AbstractValidatorVisitor {

    public ResourceValidatorMethodVisitor(Raml resourceRaml, Log logger) {
        super(resourceRaml, logger);
    }


    @Override
    public void visit(MethodDeclaration n, Object arg) {
        String path = null;
        ActionType action = null;
        if (n.getAnnotations() != null) {
            for (AnnotationExpr annotation : n.getAnnotations()) {
                if(annotation.getName().getName().equals("ExcludedFromRaml")) {
                    return;
                } else if(annotation.getName().getName().equals("Path")) {
                    path = getValue(annotation);
                }

                ActionType temp = getActionType(annotation);
                if (temp != null)
                    action = temp;
            }

            Map<String, QueryParameter> queryParams = new LinkedHashMap<String, QueryParameter>();
            if(n.getParameters() != null && !n.getParameters().isEmpty())
                queryParams = getQueryParams(n.getParameters());

            /*
             * Four cases:
             *  0: Method is not a path method
             *  1: Method has path but no action: Disallowed
             *  2: Method has action but no path: base path action assumed
             *  3: Method has path and action: add to RAML
             */
            if (path == null && action == null) {
                return;
            } else if (path != null && action == null) {
                throw new RuntimeException("Path missing action type at: " + path);
            } else if (path == null && action != null) {
                addBaseResourcePathAction(action, queryParams);
            } else {
                logger.debug("Adding path: " +path );
                addPath(path, action, queryParams);
            }
        }
    }

    private Map<String, QueryParameter> getQueryParams(List<Parameter> params) {
        Map<String, QueryParameter> queryParams = new LinkedHashMap<String, QueryParameter>();

        for(Parameter param : params) {
            if(param.getAnnotations() != null) {
                for(AnnotationExpr annotation : param.getAnnotations()) {
                    if(annotation.getName().getName().equals("QueryParam")) {
                        String name = getValue(annotation);
                        logger.debug("Adding QueryParameter: " + name);
                        QueryParameter queryParam = new QueryParameter();
                        queryParam.setDisplayName(name);
                        //queryParam.setType(ParamType.valueOf(param.getType().toString().toUpperCase()));
                        queryParams.put(name, queryParam);
                    }
                }
            }
        }

        return queryParams;
    }

    private ActionType getActionType(AnnotationExpr annotation) {
        if(annotation.getName().getName().equals("GET")) {
            return ActionType.GET;
        }
        if(annotation.getName().getName().equals("PUT")) {
            return ActionType.PUT;
        }
        if(annotation.getName().getName().equals("POST")) {
            return ActionType.POST;
        }
        if(annotation.getName().getName().equals("DELETE")) {
            return ActionType.DELETE;
        }
        if(annotation.getName().getName().equals("PATCH")) {
            return ActionType.PATCH;
        }
        if(annotation.getName().getName().equals("OPTIONS")) {
            return ActionType.OPTIONS;
        }
        if(annotation.getName().getName().equals("HEAD")) {
            return ActionType.HEAD;
        }
        if(annotation.getName().getName().equals("TRACE")) {
            return ActionType.TRACE;
        }
        return null;
    }
}
