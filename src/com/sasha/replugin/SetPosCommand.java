package com.sasha.replugin;

import com.github.steveice10.mc.protocol.data.message.TextMessage;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.sasha.reminecraft.ReMinecraft;
import com.sasha.reminecraft.client.ChildReClient;
import com.sasha.simplecmdsys.SimpleCommand;

public class SetPosCommand extends SimpleCommand {

    public SetPosCommand() {
        super("setpos");
    }

    @Override
    public void onCommand() {
        if (this.getArguments() == null || this.getArguments().length != 1) {
            return;
        }
        if (!Main.helper.resolve(this.getArguments()[0])) {
            for (ChildReClient childClient : ReMinecraft.INSTANCE.childClients) {
                childClient.getSession().send(new ServerChatPacket(new TextMessage("\2474Invalid argument. Valid example: POS_X")));
            }
        } else {
            for (ChildReClient childClient : ReMinecraft.INSTANCE.childClients) {
                childClient.getSession().send(new ServerChatPacket(new TextMessage("\247aDone!")));
            }
        }
    }
}
