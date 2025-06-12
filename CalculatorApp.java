import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class CalculatorApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorModel model = new CalculatorModel(); 
            CalculatorView view = new CalculatorView();
            new CalculatorController(model, view);
        });
    }
}

class CalculatorModel {
    private double memory;

    public String evaluate(String expression) {
        try {
            double result = eval(expression);
            return Double.toString(result);
        } catch (Exception e) {
            return "Error";
        }
    }

    public void storeToMemory(double value) {
        this.memory = value;
    }

    public double recallMemory() {
        return this.memory;
    }

    public void clearMemory() {
        this.memory = 0;
    }

    private double eval(String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }

            double parseExpression() {
                double x = parseTerm();
                while (true) {
                    if (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                while (true) {
                    if (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return -parseFactor();

                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected character: " + (char) ch);
                }

                return x;
            }
        }.parse();
    }
}

class CalculatorView {
    public JFrame frame;
    public JTextField display;
    public JTextArea historyArea;
    public JButton[] numButtons;
    public JButton[] opButtons;
    public JButton equals, clear, backspace, dot, memoryStore, memoryRecall, memoryClear;
    public JPanel panel;

    public CalculatorView() {
        frame = new JFrame("Advanced Calculator");
        frame.setSize(400, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        Color background = Color.BLACK;
        Color operatorColor = Color.YELLOW;
        Color textColor = Color.WHITE;
        Font font = new Font("Arial", Font.BOLD, 22);

        display = new JTextField();
        display.setFont(new Font("Arial", Font.BOLD, 28));
        display.setHorizontalAlignment(JTextField.RIGHT);
        display.setEditable(false);
        display.setBackground(Color.DARK_GRAY);
        display.setForeground(Color.WHITE);
        frame.add(display, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setBackground(Color.BLACK);
        historyArea.setForeground(Color.LIGHT_GRAY);
        frame.add(new JScrollPane(historyArea), BorderLayout.SOUTH);

        panel = new JPanel(new GridLayout(6, 4, 5, 5));
        panel.setBackground(background);
        numButtons = new JButton[10];
        for (int i = 0; i < 10; i++) {
            numButtons[i] = styledButton(String.valueOf(i), font, Color.GRAY, textColor);
        }

        opButtons = new JButton[]{
            styledButton("+", font, operatorColor, Color.BLACK),
            styledButton("-", font, operatorColor, Color.BLACK),
            styledButton("*", font, operatorColor, Color.BLACK),
            styledButton("/", font, operatorColor, Color.BLACK),
            styledButton("(", font, operatorColor, Color.BLACK),
            styledButton(")", font, operatorColor, Color.BLACK)
        };

        equals = styledButton("=", font, Color.GREEN, Color.BLACK);
        clear = styledButton("C", font, operatorColor, Color.BLACK);
        backspace = styledButton("<--", font, operatorColor, Color.BLACK);
        dot = styledButton(".", font, Color.GRAY, textColor);
        memoryStore = styledButton("MS", font, operatorColor, Color.BLACK);
        memoryRecall = styledButton("MR", font, operatorColor, Color.BLACK);
        memoryClear = styledButton("MC", font, operatorColor, Color.BLACK);

        JButton[] allButtons = {
            numButtons[7], numButtons[8], numButtons[9], opButtons[0],
            numButtons[4], numButtons[5], numButtons[6], opButtons[1],
            numButtons[1], numButtons[2], numButtons[3], opButtons[2],
            dot, numButtons[0], equals, opButtons[3],
            opButtons[4], opButtons[5], clear, backspace,
            memoryStore, memoryRecall, memoryClear
        };

        for (JButton b : allButtons) panel.add(b);

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private JButton styledButton(String text, Font font, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bg);
        button.setForeground(fg);
        return button;
    }
}

class CalculatorController {
    private CalculatorModel model;
    private CalculatorView view;

    public CalculatorController(CalculatorModel model, CalculatorView view) {
        this.model = model;
        this.view = view;

        for (JButton b : view.numButtons) {
            b.addActionListener(e -> view.display.setText(view.display.getText() + b.getText()));
        }

        for (JButton b : view.opButtons) {
            b.addActionListener(e -> view.display.setText(view.display.getText() + b.getText()));
        }

        view.dot.addActionListener(e -> view.display.setText(view.display.getText() + "."));

        view.equals.addActionListener(e -> {
            String result = model.evaluate(view.display.getText());
            view.historyArea.append(view.display.getText() + " = " + result + "\n");
            view.display.setText(result);
        });

        view.clear.addActionListener(e -> view.display.setText(""));

        view.backspace.addActionListener(e -> {
            String text = view.display.getText();
            if (!text.isEmpty()) view.display.setText(text.substring(0, text.length() - 1));
        });

        view.memoryStore.addActionListener(e -> {
            try {
                double val = Double.parseDouble(view.display.getText());
                model.storeToMemory(val);
            } catch (NumberFormatException ignored) {}
        });

        view.memoryRecall.addActionListener(e -> view.display.setText(String.valueOf(model.recallMemory())));
        view.memoryClear.addActionListener(e -> model.clearMemory());
    }
}
