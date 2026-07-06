package com.aruclinic.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LocationService {

    private final WebClient webClient;
    private final Map<String, LocationDetails> cache = new ConcurrentHashMap<>();

    public LocationService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.postalpincode.in")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build();
    }

    public static class LocationDetails {
        public List<String> cities = new ArrayList<>();
        public String district = "";
        public String state = "";
    }

    // Java DTO representing a single Post Office object in the API response
    public static class PostOfficeDto {
        private String name;
        private String district;
        private String state;
        private String country;
        private String pincode;

        @JsonProperty("Name")
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        @JsonProperty("District")
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }

        @JsonProperty("State")
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        @JsonProperty("Country")
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }

        @JsonProperty("Pincode")
        public String getPincode() { return pincode; }
        public void setPincode(String pincode) { this.pincode = pincode; }
    }

    // Java DTO representing the root array element of the API response
    public static class PostalResponseDto {
        private String message;
        private String status;
        private List<PostOfficeDto> postOffice;

        @JsonProperty("Message")
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        @JsonProperty("Status")
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        @JsonProperty("PostOffice")
        public List<PostOfficeDto> getPostOffice() { return postOffice; }
        public void setPostOffice(List<PostOfficeDto> postOffice) { this.postOffice = postOffice; }
    }

    private static final Map<String, LocationDetails> AP_PINCODES = new java.util.HashMap<>();
    static {
        addAP("791111", List.of("Itanagar", "Arunachal Pradesh Secretariat"), "Papum Pare");
        addAP("791110", List.of("Yupia"), "Papum Pare");
        addAP("791113", List.of("Naharlagun"), "Papum Pare");
        addAP("791102", List.of("Bomdila"), "West Kameng");
        addAP("791104", List.of("Dirang"), "West Kameng");
        addAP("791101", List.of("Bhalukpong"), "West Kameng");
        addAP("791103", List.of("Tawang"), "Tawang");
        addAP("791120", List.of("Pasighat"), "East Siang");
        addAP("791121", List.of("Mebo"), "East Siang");
        addAP("791122", List.of("Yingkiong"), "Upper Siang");
        addAP("791105", List.of("Seppa"), "East Kameng");
        addAP("791112", List.of("Ziro"), "Lower Subansiri");
        addAP("791118", List.of("Palin"), "Kra Daadi");
        addAP("791119", List.of("Koloriang"), "Kurung Kumey");
        addAP("791123", List.of("Along"), "West Siang");
        addAP("791124", List.of("Basar"), "Lepa Rada");
        addAP("791117", List.of("Daporijo"), "Upper Subansiri");
        addAP("791115", List.of("Sagalee"), "Papum Pare");
        addAP("791114", List.of("Doimukh"), "Papum Pare");
        addAP("792001", List.of("Changlang"), "Changlang");
        addAP("792056", List.of("Miao"), "Changlang");
        addAP("792055", List.of("Jairampur"), "Changlang");
        addAP("792110", List.of("Tezu"), "Lohit");
        addAP("792102", List.of("Khonsa"), "Tirap");
        addAP("792103", List.of("Longding"), "Longding");
        addAP("792104", List.of("Roing"), "Lower Dibang Valley");
        addAP("792105", List.of("Anini"), "Dibang Valley");
        addAP("792120", List.of("Namsai"), "Namsai");
        addAP("792101", List.of("Mahadevpur"), "Namsai");
        addAP("792130", List.of("Hayuliang"), "Anjaw");
    }

    private static void addAP(String pin, List<String> cities, String district) {
        LocationDetails det = new LocationDetails();
        det.cities = new ArrayList<>(cities);
        det.district = district;
        det.state = "Arunachal Pradesh";
        AP_PINCODES.put(pin, det);
    }

    public LocationDetails lookupPincode(String pincode) {
        LocationDetails details = new LocationDetails();
        if (pincode == null) {
            return details;
        }
        String pinClean = pincode.trim();
        // Validation: Must be exactly 6 digits
        if (!pinClean.matches("^\\d{6}$")) {
            return details;
        }

        // 1. Check thread-safe ConcurrentHashMap cache
        LocationDetails cachedDetails = cache.get(pinClean);
        if (cachedDetails != null) {
            return cachedDetails;
        }

        // 2. Check local AP offline fallback
        if (AP_PINCODES.containsKey(pinClean)) {
            LocationDetails ld = AP_PINCODES.get(pinClean);
            cache.put(pinClean, ld);
            return ld;
        }

        // 3. Dynamic call via WebClient
        try {
            PostalResponseDto[] response = webClient.get()
                    .uri("/pincode/{pincode}", pinClean)
                    .retrieve()
                    .bodyToMono(PostalResponseDto[].class)
                    .block(Duration.ofSeconds(10));

            if (response != null && response.length > 0) {
                PostalResponseDto resultNode = response[0];
                String status = resultNode.getStatus();
                if ("Success".equalsIgnoreCase(status)) {
                    List<PostOfficeDto> postOfficeList = resultNode.getPostOffice();
                    if (postOfficeList != null && !postOfficeList.isEmpty()) {
                        details.district = postOfficeList.get(0).getDistrict() != null ? postOfficeList.get(0).getDistrict() : "";
                        details.state = postOfficeList.get(0).getState() != null ? postOfficeList.get(0).getState() : "";
                        
                        // Extract all unique cities/post-offices under this pincode
                        details.cities = postOfficeList.stream()
                                .map(PostOfficeDto::getName)
                                .filter(name -> name != null && !name.trim().isEmpty())
                                .distinct()
                                .collect(Collectors.toList());
                        
                        // Cache response to avoid future lookups
                        cache.put(pinClean, details);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("LocationService WebClient call error for pin " + pinClean + ": " + e.getMessage());
            e.printStackTrace();
        }
        return details;
    }
}
