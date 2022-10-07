package com.testrig.simulator.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testrig.simulator.Dto.HttpRoundTripDelete;
import com.testrig.simulator.Dto.HttpRoundTripView;
import com.testrig.simulator.Dto.HttpRoundTripViewId;
import com.testrig.simulator.entity.HttpRoundTrip;
import com.testrig.simulator.exception.DataNotFoundException;
import com.testrig.simulator.exception.EmptyHttpRoundTripException;
import com.testrig.simulator.exception.InexactRequestBodyException;
import com.testrig.simulator.exception.NoConfiguredPathException;
import com.testrig.simulator.repo.HttpRoundTripRepo;
import com.testrig.simulator.service.HttpRoundTripService;

@Service
public class HttpRoundTripServiceImpl implements HttpRoundTripService {

    @Autowired
    HttpRoundTripRepo httpRoundTripRepo;

    @Autowired
    ObjectMapper objectMapper;

    @Override
    public String resFinder(Object obj) throws JsonProcessingException {

        String s = objectMapper.writeValueAsString(obj);
        HttpRoundTrip req = httpRoundTripRepo.findByRequest(s);
        return req.getResponse();
    }

    @Override
    public void addRequestResponse(HttpRoundTripView requestView) throws JsonProcessingException {

        HttpRoundTrip httpRoundTrip = httpRoundTripRepo.findByPathAndRequest("/api"+requestView.getPath(),
                objectMapper.writeValueAsString(requestView.getRequest()));

        if (httpRoundTrip != null) {

            httpRoundTrip.setResponse(objectMapper.writeValueAsString(requestView.getResponse()));
            httpRoundTripRepo.save(httpRoundTrip);

        } else {

            HttpRoundTrip req = new HttpRoundTrip();
            req.setRequest(objectMapper.writeValueAsString(requestView.getRequest()));
            req.setResponse(objectMapper.writeValueAsString(requestView.getResponse()));

            req.setPath("/api"+requestView.getPath());

            httpRoundTripRepo.save(req);

        }

    }

    @Override
    public void deleteHttpRoundTrip(HttpRoundTripDelete object) throws JsonProcessingException {

        HttpRoundTrip r = httpRoundTripRepo.findByPathAndRequest("/api"+object.getPath(),
                objectMapper.writeValueAsString(object.getRequest()));
        if (r != null) {
            httpRoundTripRepo.delete(r);

        } else {

            throw new DataNotFoundException();

        }

    }

    @Override
    public List<HttpRoundTripViewId> allRequestResponse() {

        List<HttpRoundTripViewId> httpRoundTripViews = new ArrayList<>();

        List<HttpRoundTrip> httpRoundTrips = httpRoundTripRepo.findAll();
        if( httpRoundTrips.isEmpty()){
            throw new EmptyHttpRoundTripException();
        }
        httpRoundTrips.forEach(e -> {
            try {
                httpRoundTripViews.add(jSONformaterViewId(e));
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
            }
        });

        return httpRoundTripViews;
    }

    @Override
    public HttpRoundTripView findReqResByPath(String requestURI, Object obj) throws JsonProcessingException {

        List<HttpRoundTrip> eList = httpRoundTripRepo.findByPath(requestURI);

        if (eList.size() <= 0) {

            throw new NoConfiguredPathException();

        }

        String s = objectMapper.writeValueAsString(obj);
        for (HttpRoundTrip httpRoundTrip : eList) {

            if (httpRoundTrip.getRequest().equals(s)) {

                return jSONformaterView(httpRoundTrip);
            } else {

                throw new InexactRequestBodyException();
            }

        }

        return null;
    }

    public HttpRoundTripView jSONformaterView(HttpRoundTrip httpRoundTrip)
            throws JsonMappingException, JsonProcessingException {
        HttpRoundTripView httpRoundTripView = new HttpRoundTripView();
        httpRoundTripView.setPath(httpRoundTrip.getPath());
        httpRoundTripView.setRequest(objectMapper.readValue(httpRoundTrip.getRequest(), Object.class));
        httpRoundTripView.setResponse(objectMapper.readValue(httpRoundTrip.getResponse(), Object.class));
        return httpRoundTripView;
    }

    public HttpRoundTripViewId jSONformaterViewId(HttpRoundTrip httpRoundTrip)
            throws JsonMappingException, JsonProcessingException {
        HttpRoundTripViewId httpRoundTripView = new HttpRoundTripViewId();
        httpRoundTripView.setId(httpRoundTrip.getId());
        httpRoundTripView.setPath(httpRoundTrip.getPath());
        httpRoundTripView.setRequest(objectMapper.readValue(httpRoundTrip.getRequest(), Object.class));
        httpRoundTripView.setResponse(objectMapper.readValue(httpRoundTrip.getResponse(), Object.class));
        return httpRoundTripView;
    }

    @Override
    public void deleteHttpRoundTripById(Integer id) {

        httpRoundTripRepo.deleteById(id);

    }
}
