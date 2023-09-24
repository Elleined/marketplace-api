package com.elleined.marketplaceapi.service.message.prv;

import com.elleined.marketplaceapi.exception.resource.ResourceNotFoundException;
import com.elleined.marketplaceapi.model.Product;
import com.elleined.marketplaceapi.model.message.prv.PrivateChatMessage;
import com.elleined.marketplaceapi.model.message.prv.PrivateChatRoom;
import com.elleined.marketplaceapi.model.user.User;

import java.util.List;

public interface PrivateChatRoomService {

    PrivateChatRoom getChatRoom(User sender, User receiver, Product productToSettle) throws ResourceNotFoundException;

    PrivateChatRoom getChatRoom(int roomId) throws ResourceNotFoundException;

    List<PrivateChatMessage> getAllPrivateMessage(PrivateChatRoom privateChatRoom);

    PrivateChatRoom getOrCreateChatRoom(User sender, User receiver, Product productToSettle);
}
