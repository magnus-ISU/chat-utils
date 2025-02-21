package io.github.hotlava03.chatutils.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.message.ChatMessageSigner;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

@Mixin(ClientPlayerEntity.class)
public class CopyToClipboardMixin {

    String last_chat_message = "";

    @Inject(method = "sendCommand(Lnet/minecraft/network/message/ChatMessageSigner;Ljava/lang/String;Lnet/minecraft/text/Text;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onSendCommand(ChatMessageSigner signer, String command, Text preview, CallbackInfo ci) {
        if (command.startsWith("chatmacros ")) {
            ci.cancel();

            String inform_player = "Text copied.";

            if (!command.equals(last_chat_message)) {
                last_chat_message = command;
                // Search in message for the first URL and copy only that
                String[] words = command.split(" ");
                for (String word : words) {
                    if (word.startsWith("https://") || word.startsWith("http://")) {
                        last_chat_message = command;
                        command = word;
                        inform_player = "URL copied. Click again to copy entire message";
                        break;
                    }
                }
            }

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(command.replaceFirst("chatmacros ", "").replace("§", "&")),
                    null
            );
            // MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(new LiteralText("\u00a79ChatUtils \u00a78\u00BB \u00a77Copied to clipboard."));
            SystemToast.show(MinecraftClient.getInstance().getToastManager(),
                    SystemToast.Type.WORLD_GEN_SETTINGS_TRANSFER,
                    Text.literal("ChatUtils"),
                    Text.literal(inform_player)
            );
        }
    }
}
