package com.example.ems.controller;

import com.example.ems.domain.employee.EmployeeProfile;
import com.example.ems.domain.shift.Shift;
import com.example.ems.domain.shift.ShiftSwapRequest;
import com.example.ems.repository.EmployeeProfileRepository;
import com.example.ems.service.ShiftService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/my")
public class MyDashboardController {

    private final ShiftService shiftService;
    private final EmployeeProfileRepository employeeRepo;

    public MyDashboardController(ShiftService shiftService, EmployeeProfileRepository employeeRepo) {
        this.shiftService = shiftService;
        this.employeeRepo = employeeRepo;
    }

    @GetMapping
    public String dashboard(Authentication auth, Model model) {
        if (auth == null) {
            return "redirect:/login";
        }

        boolean isAdmin = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(a -> a.equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/";
        }

        EmployeeProfile me = findEmployeeByUsername(auth.getName());
        if (me == null) {
            model.addAttribute("message", "User profile not found");
            return "login-required";
        }

        List<Shift> myShifts = shiftService.getShiftsByEmployee(me.getId());

        List<ShiftSwapRequest> myRequestedSwaps = shiftService.getSwapRequestsByRequester(me.getId());
        List<ShiftSwapRequest> myTargetedSwaps = shiftService.getSwapRequestsByTarget(me.getId());

        List<ShiftSwapRequest> myPendingSwaps =
            concat(myRequestedSwaps, myTargetedSwaps).stream()
                .filter(ShiftSwapRequest::isPending)
                .collect(Collectors.toList());

        Map<String, Object> stats = Map.of(
            "myTotalShifts", myShifts.size(),
            "myPendingSwaps", (long) myPendingSwaps.size()
        );

        model.addAttribute("me", me);
        model.addAttribute("myShifts", myShifts);
        model.addAttribute("myPendingSwaps", myPendingSwaps);
        model.addAttribute("statistics", stats);
        return "my/dashboard";
    }

    private EmployeeProfile findEmployeeByUsername(String username) {
        var byEmail = employeeRepo.findByEmail(username + "@example.com");
        if (byEmail.isPresent()) {
            return byEmail.get();
        }
        var byFirstName = employeeRepo.findByFirstNameIgnoreCase(username);
        if (byFirstName.isPresent()) {
            return byFirstName.get();
        }
        return employeeRepo.findAll().stream().findFirst().orElse(null);
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).collect(Collectors.toList());
    }
}


