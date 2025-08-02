package daniel.text_block.client.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NumberTextFieldWidget extends TextFieldWidget {
    public NumberTextFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text) {
        super(textRenderer, x, y, width, height, text);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        // Get the text that would result after adding this character
        String currentText = this.getText();
        int cursor = this.getCursor();
        String newText = currentText.substring(0, cursor) + chr + currentText.substring(cursor);

        // Allow valid intermediate states for number input
        if (isValidNumberInput(newText)) {
            return super.charTyped(chr, modifiers);
        }

        return false;
    }

    private boolean isValidNumberInput(String text) {
        if (text.isEmpty()) {
            return true;
        }

        // Regex for valid number input (including intermediate states)
        // Allows: empty, -, +, ., -.5, -123, 123.456, etc.
        return text.matches("^[+-]?(?:\\d*\\.?\\d*)?$");
    }
}
