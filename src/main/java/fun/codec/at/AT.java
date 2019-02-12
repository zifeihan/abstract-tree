package fun.codec.at;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AT extends Application {

    private TreeItem<String> root = new TreeItem<>();

    private static String filePath = null;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Java language Syntax Tree Viewer");
        root.setExpanded(true);
        TreeView<String> treeView = new TreeView<>(root);
        StackPane root = new StackPane();
        root.getChildren().add(treeView);
        primaryStage.setScene(new Scene(root, 500, 600));
        primaryStage.show();
        parser();
    }

    public static void main(String[] args) {
        filePath = args[0];
        launch(args);
    }

    private void parser() {
        Context context = new Context();
        JavaCompiler javaCompiler = new JavaCompiler(context);
        JCTree.JCCompilationUnit jcCompilationUnit = javaCompiler.parse(filePath);
        root.setValue(jcCompilationUnit.getSourceFile().getName());
        TreeItem treeItem = buildTreeItem(jcCompilationUnit, root);
    }


    private TreeItem buildTreeItem(Object object, TreeItem<String> root) {
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                field.setAccessible(true);
                if (field.get(object) instanceof List) {
                    List list = (List) field.get(object);
                    for (int i = 0; i < list.size(); i++) {
                        Object o = list.get(i);
                        if (o instanceof JCTree) {
                            TreeItem<String> branch = new TreeItem<>(o.getClass().getSimpleName() + ": " + replaceBlank(o.toString()));
                            root.getChildren().add(branch);
                            //build new branch
                            buildTreeItem(o, branch);
                        }
                    }
                }
                if (field.get(object) instanceof JCTree) {
                    JCTree o = (JCTree) field.get(object);

                    //build new branch
                    TreeItem<String> branch = new TreeItem<>(field.getName() + ": " + replaceBlank(o.toString()));
                    root.getChildren().add(branch);

                    buildTreeItem(o, branch);
                }
                if (field.get(object) instanceof Name) {
                    Name o = (Name) field.get(object);

                    TreeItem<String> item = new TreeItem<>(field.getName() + ": " + o.toString());
                    root.getChildren().add(item);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return root;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\r|\\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}