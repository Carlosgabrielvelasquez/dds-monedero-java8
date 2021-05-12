package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo = 0;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double montoPorPoner) {
    elMontoIngresadoEsPositivo(montoPorPoner);
    disponeDeDepositosDiarios();

    this.agregarMovimiento(LocalDate.now(), montoPorPoner, true);
  }

  public void sacar(double montoPorSacar) {
    elMontoIngresadoEsPositivo(montoPorSacar);
    haySaldoSuficienteParaSacar(montoPorSacar);
    noSeSuperaElLimiteDiarioDeExtraccion(montoPorSacar);

    agregarMovimiento(LocalDate.now(), montoPorSacar, false);

  }

  private void elMontoIngresadoEsPositivo(double monto){
    if (monto <= 0) {
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  private void noSeSuperaElLimiteDiarioDeExtraccion(double monto) {
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    double limite = 1000 - montoExtraidoHoy;
    if (monto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
          + " diarios, límite: " + limite);
    }
  }

  private void haySaldoSuficienteParaSacar (double monto) {
    if (getSaldo() - monto < 0) {
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  private void disponeDeDepositosDiarios(){
    if (getMovimientos().stream().filter(movimiento -> movimiento.fueDepositado(LocalDate.now())).count() >= 3) {
      throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
    }
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento nuevoMovimiento = new Movimiento(fecha, cuanto, esDeposito);
    efectuarMovimiento(nuevoMovimiento);
    movimientos.add(nuevoMovimiento);
  }
  private void efectuarMovimiento (Movimiento movimiento) {
    if (movimiento.isDeposito())
      this.saldo = this.saldo + movimiento.getMonto();
    else
      this.saldo = this.saldo - movimiento.getMonto();
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> movimiento.fueExtraido(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

}
