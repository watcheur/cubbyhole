<?php

namespace Entities;

use Doctrine\ORM\Mapping as ORM;

/**
 * Entities\Plan
 */
class Plan
{
    /**
     * @var integer $id
     */
    private $id;

    /**
     * @var string $name
     */
    private $name;

    /**
     * @var float $price
     */
    private $price;

    /**
     * @var integer $duration
     */
    private $duration;

    /**
     * @var integer $usable_storage_space
     */
    private $usable_storage_space;

    /**
     * @var integer $max_bandwidth
     */
    private $max_bandwidth;

    /**
     * @var integer $daily_data_transfert
     */
    private $daily_data_transfert;

    /**
     * @var \Doctrine\Common\Collections\ArrayCollection
     */
    private $plan_historys;

    public function __construct()
    {
        $this->plan_historys = new \Doctrine\Common\Collections\ArrayCollection();
    }
    
    /**
     * Get id
     *
     * @return integer 
     */
    public function getId()
    {
        return $this->id;
    }

    /**
     * Set name
     *
     * @param string $name
     * @return Plan
     */
    public function setName($name)
    {
        $this->name = $name;
        return $this;
    }

    /**
     * Get name
     *
     * @return string 
     */
    public function getName()
    {
        return $this->name;
    }

    /**
     * Set price
     *
     * @param float $price
     * @return Plan
     */
    public function setPrice($price)
    {
        $this->price = $price;
        return $this;
    }

    /**
     * Get price
     *
     * @return float 
     */
    public function getPrice()
    {
        return $this->price;
    }

    /**
     * Set duration
     *
     * @param integer $duration
     * @return Plan
     */
    public function setDuration($duration)
    {
        $this->duration = $duration;
        return $this;
    }

    /**
     * Get duration
     *
     * @return integer 
     */
    public function getDuration()
    {
        return $this->duration;
    }

    /**
     * Set usable_storage_space
     *
     * @param integer $usableStorageSpace
     * @return Plan
     */
    public function setUsableStorageSpace($usableStorageSpace)
    {
        $this->usable_storage_space = $usableStorageSpace;
        return $this;
    }

    /**
     * Get usable_storage_space
     *
     * @return integer 
     */
    public function getUsableStorageSpace()
    {
        return $this->usable_storage_space;
    }

    /**
     * Set max_bandwidth
     *
     * @param integer $maxBandwidth
     * @return Plan
     */
    public function setMaxBandwidth($maxBandwidth)
    {
        $this->max_bandwidth = $maxBandwidth;
        return $this;
    }

    /**
     * Get max_bandwidth
     *
     * @return integer 
     */
    public function getMaxBandwidth()
    {
        return $this->max_bandwidth;
    }

    /**
     * Set daily_data_transfert
     *
     * @param integer $dailyDataTransfert
     * @return Plan
     */
    public function setDailyDataTransfert($dailyDataTransfert)
    {
        $this->daily_data_transfert = $dailyDataTransfert;
        return $this;
    }

    /**
     * Get daily_data_transfert
     *
     * @return integer 
     */
    public function getDailyDataTransfert()
    {
        return $this->daily_data_transfert;
    }

    /**
     * Add plan_historys
     *
     * @param Entities\PlanHistory $planHistorys
     * @return Plan
     */
    public function addPlanHistory(\Entities\PlanHistory $planHistorys)
    {
        $this->plan_historys[] = $planHistorys;
        return $this;
    }

    /**
     * Remove plan_historys
     *
     * @param Entities\PlanHistory $planHistorys
     */
    public function removePlanHistory(\Entities\PlanHistory $planHistorys)
    {
        $this->plan_historys->removeElement($planHistorys);
    }

    /**
     * Get plan_historys
     *
     * @return Doctrine\Common\Collections\Collection 
     */
    public function getPlanHistorys()
    {
        return $this->plan_historys;
    }
}