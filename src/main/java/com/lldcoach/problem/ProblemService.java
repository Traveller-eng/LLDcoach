package com.lldcoach.problem;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class containing problem-related business logic.
 * Provides problems to the controller.
 */
@Service
public class ProblemService {

    private final List<Problem> problems;

    public ProblemService() {
        this.problems = initializeProblems();
    }

    /**
     * Initialize the three seeded LLD problems.
     */
    private List<Problem> initializeProblems() {
        return List.of(
            createParkingLotProblem(),
            createVendingMachineProblem(),
            createElevatorSystemProblem()
        );
    }

    private Problem createParkingLotProblem() {
        return new Problem(
            "Parking Lot",
            "Design a parking lot system that can manage vehicles entering and leaving a parking facility.",
            Difficulty.EASY,
            List.of(
                "The parking lot can contain multiple floors.",
                "Each floor contains multiple parking spots.",
                "Different vehicle types such as cars and motorcycles should be supported.",
                "Different parking spot types should be supported.",
                "The system should assign a suitable available parking spot to a vehicle.",
                "The system should generate a parking ticket when a vehicle enters.",
                "The system should calculate the parking fee when the vehicle exits.",
                "The design should allow different pricing strategies to be introduced later."
            )
        );
    }

    private Problem createVendingMachineProblem() {
        return new Problem(
            "Vending Machine",
            "Design a vending machine that allows customers to select products, insert money, purchase products, and receive change.",
            Difficulty.MEDIUM,
            List.of(
                "The machine contains multiple products.",
                "Products have prices and quantities.",
                "A customer can select a product.",
                "A customer can insert money.",
                "The machine should reject invalid transactions.",
                "The machine should return appropriate change.",
                "The machine should handle unavailable products.",
                "The design should allow the machine's behavior/state to change during a transaction."
            )
        );
    }

    private Problem createElevatorSystemProblem() {
        return new Problem(
            "Elevator System",
            "Design an elevator system that manages elevator requests in a multi-floor building.",
            Difficulty.MEDIUM,
            List.of(
                "The building contains multiple floors.",
                "The system can contain one or more elevators.",
                "Users can request an elevator from a floor.",
                "Users can select a destination floor.",
                "The system should decide which elevator should handle a request.",
                "An elevator should maintain its current floor and direction.",
                "The system should handle multiple requests.",
                "The design should allow the elevator selection/scheduling strategy to change later."
            )
        );
    }

    /**
     * Get all available problems.
     */
    public List<Problem> getAllProblems() {
        return problems;
    }

    /**
     * Get a problem by ID.
     * Returns Optional.empty() if the problem doesn't exist.
     */
    public Optional<Problem> getProblemById(String id) {
        return problems.stream()
                .filter(problem -> problem.getId().equals(id))
                .findFirst();
    }
}
