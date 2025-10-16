package com.example.trainbookingsystem;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.trainbookingsystem.entity.Ticket;
import com.example.trainbookingsystem.entity.Train;
import com.example.trainbookingsystem.entity.User;
import com.example.trainbookingsystem.repository.TicketRepository;
import com.example.trainbookingsystem.service.TicketService;
import com.example.trainbookingsystem.service.TrainService;
import com.example.trainbookingsystem.service.UserService;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserService userService;

    @Mock
    private TrainService trainService;

    @InjectMocks
    private TicketService ticketService;

    private Ticket ticket;
    private User user;
    private Train train;

    @BeforeEach
    void setUp() {
        
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");

        
        train = new Train();
        train.setId(1L);
        train.setName("Rajdhani Express");
        train.setSource("New Delhi");
        train.setDestination("Mumbai Central");
        train.setBasePrice(1500.00);
        train.setDiscountPercentage(10.0);

      
        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setUser(user);
        ticket.setTrain(train);
        ticket.setBookingDate(LocalDateTime.now());
        ticket.setFinalPrice(1350.00); 
    }

    @Test
    void getAllTickets_ShouldReturnListOfTickets() {
        List<Ticket> tickets = Arrays.asList(ticket);
        when(ticketRepository.findAll()).thenReturn(tickets);

        List<Ticket> result = ticketService.getAllTickets();

        assertEquals(1, result.size());
        assertEquals(ticket.getId(), result.get(0).getId());
        assertEquals(ticket.getUser().getName(), result.get(0).getUser().getName());
        verify(ticketRepository, times(1)).findAll();
    }

    @Test
    void getTicketById_WhenTicketExists_ShouldReturnTicket() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

        Optional<Ticket> result = ticketService.getTicketById(1L);

        assertTrue(result.isPresent());
        assertEquals(ticket.getId(), result.get().getId());
        assertEquals(ticket.getUser().getName(), result.get().getUser().getName());
        assertEquals(ticket.getTrain().getName(), result.get().getTrain().getName());
        verify(ticketRepository, times(1)).findById(1L);
    }

    @Test
    void getTicketById_WhenTicketDoesNotExist_ShouldReturnEmpty() {
        when(ticketRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Ticket> result = ticketService.getTicketById(1L);

        assertFalse(result.isPresent());
        verify(ticketRepository, times(1)).findById(1L);
    }

    @Test
    void createTicket_ShouldSaveAndReturnTicket() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));
        when(trainService.getTrainById(1L)).thenReturn(Optional.of(train));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.createTicket(1L, 1L);

        assertNotNull(result);
        assertEquals(ticket.getId(), result.getId());
        assertEquals(ticket.getUser().getName(), result.getUser().getName());
        assertEquals(ticket.getTrain().getName(), result.getTrain().getName());
        assertEquals(1350.00, result.getFinalPrice(), 0.01); // Verify calculated price
        verify(userService, times(1)).getUserById(1L);
        verify(trainService, times(1)).getTrainById(1L);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void updateTicket_WhenTicketExists_ShouldUpdateAndReturnTicket() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setName("Jane Doe");
        newUser.setEmail("jane.doe@example.com");

        Train newTrain = new Train();
        newTrain.setId(2L);
        newTrain.setName("Shatabdi Express");
        newTrain.setSource("Chennai");
        newTrain.setDestination("Bangalore");
        newTrain.setBasePrice(800.00);
        newTrain.setDiscountPercentage(5.0);

        Ticket updatedTicket = new Ticket();
        updatedTicket.setUser(newUser);
        updatedTicket.setTrain(newTrain);

        when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.updateTicket(1L, updatedTicket);

        assertNotNull(result);
        assertEquals(newUser.getName(), result.getUser().getName());
        assertEquals(newTrain.getName(), result.getTrain().getName());
        verify(ticketRepository, times(1)).findById(1L);
        verify(ticketRepository, times(1)).save(any(Ticket.class));
    }

    @Test
    void deleteTicket_ShouldDeleteTicket() {
        doNothing().when(ticketRepository).deleteById(1L);

        ticketService.deleteTicket(1L);

        verify(ticketRepository, times(1)).deleteById(1L);
    }

    @Test
    void calculateTicketPrice_ShouldReturnCorrectPrice() {
        double basePrice = 1500.00;
        double discountPercentage = 10.0;

        double result = ticketService.calculateTicketPrice(basePrice, discountPercentage);

        assertEquals(1350.00, result, 0.01);
    }

    @Test
    void calculateTicketPrice_WithNoDiscount_ShouldReturnBasePrice() {
        double basePrice = 1500.00;
        double discountPercentage = 0.0;

        double result = ticketService.calculateTicketPrice(basePrice, discountPercentage);

        assertEquals(1500.00, result, 0.01);
    }
}