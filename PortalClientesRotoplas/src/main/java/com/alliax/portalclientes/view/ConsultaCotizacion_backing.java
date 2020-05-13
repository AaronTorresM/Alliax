package com.alliax.portalclientes.view;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


import com.alliax.portalclientes.domain.*;
import com.alliax.portalclientes.model.DetallePedidoCotizacion;
import com.alliax.portalclientes.service.MaterialService;
import org.apache.log4j.Logger;

import com.alliax.portalclientes.model.CotizacionFlete;
import com.alliax.portalclientes.service.PedidoPartidasService;
import com.alliax.portalclientes.service.PedidoService;

@ManagedBean(name="consultaCotizacion")
@SessionScoped
public class ConsultaCotizacion_backing extends AbstractBackingGen {

    private final static Logger logger = Logger.getLogger(ConsultaCotizacion_backing.class);
    private String noCotizacion;
    private String fecha;
    private String noCliente;

    private PedidoService service;
    private PedidoPartidasService partidaService;
    private MaterialService materialService;
    private List<CotizacionFlete> cotizaciones;

    private List<DetallePedidoCotizacion> partidas;
    
    private DetallePedidoCotizacion cotizacion;

    public String getNoCotizacion() {
        return noCotizacion;
    }

    public void setNoCotizacion(String noCotizacion) {
        this.noCotizacion = noCotizacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNoCliente() {
        return noCliente;
    }

    public void setNoCliente(String noCliente) {
        this.noCliente = noCliente;
    }

    public List<CotizacionFlete> getCotizaciones() {
        return cotizaciones;
    }

    public void setCotizaciones(List<CotizacionFlete> cotizaciones) {
        this.cotizaciones = cotizaciones;
    }

    public List<DetallePedidoCotizacion> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<DetallePedidoCotizacion> partidas) {
        this.partidas = partidas;
    }
    
    public DetallePedidoCotizacion getCotizacion() {
    	return this.cotizacion;
    }
    public void setCotizacion(DetallePedidoCotizacion cotizacion) {
    	this.cotizacion = cotizacion;
    	
    }
    @PostConstruct
    public void init(){

        logger.info("consultaCotizacion init");
        try{
            service = this.getSpringContext().getBean("pedidoService" , PedidoService.class);
            partidaService = this.getSpringContext().getBean("pedidoPartidasService" , PedidoPartidasService.class);
            materialService = this.getSpringContext().getBean("materialService" , MaterialService.class);
        }catch(Exception e){
            logger.error("Error al iniciar");
        }
    }

    public String buscaCotizaciones(String noCliente){
        logger.info("consultaCotizacion " + fecha + noCliente + noCotizacion);
        cotizaciones = new ArrayList();
        try{

             List<com.alliax.portalclientes.domain.Pedido> pedidos = service.findCotizacionesFlete(fecha , noCotizacion ,noCliente);
             for(com.alliax.portalclientes.domain.Pedido p : pedidos){
                        CotizacionFlete c = new CotizacionFlete();
                        c.setNroPedido(""+p.getIdPedido());
                        c.setNoCotizacion(p.getNoCotizacion());
                        c.setEstado(p.getEstatusCotizacion());
                        PedidoPartidasPK pk = new PedidoPartidasPK();
                        pk.setIdPedido(p.getIdPedido());
                        pk.setSku(CotizacionFlete.idMatFlete);
                        com.alliax.portalclientes.domain.PedidoPartidas pp = partidaService.findById(pk);
                        if(pp!= null) {
                        	c.setMaterial(CotizacionFlete.idMatFlete);
                        	c.setCantidad(pp.getCantidad());
                        	c.setDescripcion("Flete");
                        	c.setPrecioNeto(pp.getPrecioNeto());
                        	c.setMonto(pp.getMonto());
                        	c.setuM("SVO");
                        }
                        Material mat = materialService.findById(CotizacionFlete.idMatFlete);
                        if(mat!= null){
                            c.setMaterial(mat.getSku());
                            c.setDescripcion(mat.getDescripcion());
                            c.setuM(mat.getUnidadMedida());
                        }
                        this.getCotizaciones().add(c);
                    }
        } catch(Exception e){
            logger.error("Error al desplegar listado de fletes " + e.getLocalizedMessage());
            this.getFacesContext().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,"Error",this.getLblMain().getString("errListaPedidos")));
        }
        return "";
    }

    public String buscarDetalles(String noPedido){
        partidas = new ArrayList();
        try{
            Long idPedido = Long.valueOf(noPedido);
            Pedido pedido=  service.findById(idPedido);
           List<PedidoPartidas> partidasPedidos = partidaService.findByidPedido(idPedido);
            for(PedidoPartidas pp : partidasPedidos){
            	
                DetallePedidoCotizacion d = new DetallePedidoCotizacion();
                d.setNoPedido(noPedido);
                d.setPosicion(pp.getPosicion());
                Material mat = materialService.findById(pp.getId().getSku());
                if(mat!= null){
                    d.setNoMaterial(mat.getSku());
                    d.setDescripcion(mat.getDescripcion());
                    d.setUnidadMedida(mat.getUnidadMedida());
                }
                d.setMonto(pp.getMonto());
                d.setPrecioNeto(pp.getPrecioNeto());
                d.setCantidad(pp.getCantidad());
                d.setFechaEnt(pp.getFechaEntrega());
                d.setEstatus(pedido.getEstatusCotizacion());
                d.setCantEnt(pp.getCantidadEntregada());
                
                if(pp.getId().getSku().equals(CotizacionFlete.idMatFlete)) {
                	this.noCotizacion = pedido.getNoCotizacion();
                	this.cotizacion = d;
                }else {
                	partidas.add(d);
                }
                
            }
        } catch(Exception e){
            logger.error("Error al desplegar listado de fletes " + e.getLocalizedMessage());
            this.getFacesContext().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,"Error",this.getLblMain().getString("errListaPedidos")));
        }
        return "";
    }


}