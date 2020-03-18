/**
*  Efergy Engage Energy
*
*  Copyright 2016, 2017, 2018 Anthony S.
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  ---------------------------
*/

import java.text.SimpleDateFormat

def devTypeVer() {"3.3.1"}
def versionDate() {"3-18-2020"}

metadata {
    definition (name: "Efergy Engage Elite", namespace: "tonesto7", author: "Anthony S.") {
        capability "Energy Meter"
        capability "Power Meter"
        capability "Polling"
        capability "Refresh"
        capability "Actuator"
        capability "Sensor"
        capability "Health Check"

        attribute "todayUsage", "string"
        attribute "todayCost", "string"
        attribute "weekUsage", "string"
        attribute "weekCost", "string"
        attribute "monthUsage", "string"
        attribute "monthCost", "string"
        attribute "monthEst", "string"
        attribute "yearUsage", "string"
        attribute "yearCost", "string"
        attribute "maxPowerReading", "string"
        attribute "minPowerReading", "string"
        attribute "maxEnergyReading", "string"
        attribute "minEnergyReading", "string"
        attribute "dayPowerAvg", "string"
        attribute "readingUpdated", "string"
        attribute "apiStatus", "string"
        attribute "devTypeVer", "string"

        command "poll"
        command "refresh"
    }

    tiles (scale: 2) {
        multiAttributeTile(name:"powerMulti", type:"generic", width:6, height:4) {
            tileAttribute("device.power", key: "PRIMARY_CONTROL") {
                attributeState "power", label: '${currentValue}W', unit: "W", icon: "https://raw.githubusercontent.com/tonesto7/efergy-manager/master/resources/images/power_icon_bk.png",
                        foregroundColor: "#000000",
                        backgroundColors:[
                            [value: 1, color: "#00cc00"], //Light Green
                            [value: 2000, color: "#79b821"], //Darker Green
                            [value: 3000, color: "#ffa81e"], //Orange
                            [value: 4000, color: "#FFF600"], //Yellow
                            [value: 5000, color: "#fb1b42"] //Bright Red
                        ]
            }
            tileAttribute("todayUsage_str", key: "SECONDARY_CONTROL") {
                      attributeState "default", label: 'Today\'s Usage: ${currentValue}'
            }
        }

        valueTile("todayUsage_str", "device.todayUsage_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Today\'s Usage:\n${currentValue}'
        }

        valueTile("monthUsage_str", "device.monthUsage_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }

        valueTile("monthEst_str", "device.monthEst_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }

        valueTile("budgetPercentage_str", "device.budgetPercentage_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }

        valueTile("tariffRate", "device.tariffRate", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Tariff Rate:\n${currentValue}/kWH'
        }
        valueTile("tariffRate_str", "device.tariffRate_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: '${currentValue}'
        }
        valueTile("hubStatus", "device.hubStatus", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Hub Status:\n${currentValue}'
        }

        valueTile("pwrMin", "device.minPowerReading", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Power (Min):\n${currentValue}W'
        }
        valueTile("pwrAvg", "device.dayPowerAvg", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Power (Avg):\n${currentValue}W'
        }
        valueTile("pwrMax", "device.maxPowerReading", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Power (Max):\n${currentValue}W'
        }
        valueTile("hubVersion", "device.hubVersion", width: 2, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Hub Version:\n${currentValue}'
        }
        valueTile("readingUpdated_str", "device.readingUpdated_str", width: 3, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label:'${currentValue}'
        }

        standardTile("refresh", "command.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }

        valueTile("devVer", "device.devTypeVer", width: 4, height: 1, decoration: "flat", wordWrap: true) {
            state "default", label: 'Device Type Version:\nv${currentValue}'
        }
        htmlTile(name:"graphHTML", action: "getGraphHTML", width: 6, height: 10, whitelist: ["www.gstatic.com", "raw.githubusercontent.com", "cdn.rawgit.com"])

        main (["powerMulti"])
        details(["powerMulti", "todayUsage_str", "monthUsage_str", "monthEst_str", "budgetPercentage_str", "tariffRate_str", "readingUpdated_str", "pwrMin", "pwrAvg", "pwrMax", "graphHTML", "refresh"])
    }
}

preferences {
    input "resetHistoryData", "bool", title: "Reset History Data", description: "", displayDuringSetup: false
}

mappings {
    path("/getGraphHTML") {action: [GET: "getGraphHTML"]}
}

// parse events into attributes
def parse(String description) {
    logWriter("Parsing '${description}'")
}

void checkStateClear() {
	//Logger("checkStateClear...")
    if(state?.resetHistoryData == null) { state?.resetHistoryData = false }
    if(state?.resetHistoryData == false  && settings?.resetHistoryData == true) {
        runIn(4, "clearAllState", [overwrite:true])
	} else if(state?.resetHistoryData == true && settings?.resetHistoryData == false) {
		log.debug("checkStateClear...resetting HISTORY toggle")
		state?.resetHistoryData = false
        //device.updateSetting("resetHistoryData", "false")
	}
}

void clearAllState() {
    def data = getState()?.findAll { !(it?.key in ["resetHistoryData"]) }
    log.debug "checkStateClear removing ${data?.size()} state variables"
    def before = getStateSizePerc()
    data?.each { item ->
        state.remove(item?.key.toString())
    }
    state?.resetHistoryData = true
    //log.debug("Device State Data: Before: $before | After: ${getStateSizePerc()}")
}

void refresh() {
    poll()
}

// Poll command
void poll() {
    log.info "Poll command received..."
    parent?.poll()
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"
 	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(), displayed: false)
}

void generateEvent(Map eventData) {
    //log.trace("generateEvent Parsing data ${eventData}")
    checkStateClear()
    if(eventData) {
        // log.debug "eventData: $eventData"
        state?.monthName = eventData?.monthName
        state?.currency = eventData?.currency
        debugOnEvent(eventData?.debug ? true : false)
        deviceVerEvent(eventData?.latestVer.toString())
        updateAttributes(eventData?.readingData, eventData?.usageData, eventData?.tariffData, eventData?.hubData)
        handleData(eventData?.readingData, eventData?.usageData)
        apiStatusEvent(eventData?.apiIssues)
        lastCheckinEvent(eventData?.hubData?.hubTsHuman)
    }
    lastUpdatedEvent()
}

private handleData(Map readingData, Map usageData) {
    //log.trace "handleData ($localTime, $power, $energy)"
    try {
        def today = new Date()
        def currentHour = today?.format("HH", location?.timeZone)?.toInteger()
        def currentDay = today?.format("dd", location?.timeZone) //1...31
        def currentDayNum = today?.format("u", location?.timeZone)?.toInteger() // 1 = Monday,... 7 = Sunday
        def currentMonth = today?.format("MM", location?.timeZone)
        def currentYear = today?.format("YYYY", location?.timeZone)?.toInteger()
        if(!state?.currentDay) { state?.currentDay = currentDay }
        if(!state?.currentDayNum) { state?.currentDayNum = currentDayNum }
        if(!state?.currentYear) { state?.currentYear = currentYear }
        if(!state?.currentMonth) { state?.currentMonth = currentMonth }
        def currentEnergy = usageData?.todayUsage as Float
        def currentPower = readingData?.powerReading ?: 0L
        // currentPower = currentPower?.toFloat() < 1.0 ? 1 : currentPower
        // log.debug("currentHour: $currentHour | (state): ${state?.currentHour}")
        // log.debug("currentDay: $currentDay | (state): ${state?.currentDay}")
        // log.debug("currentDayNum: $currentDayNum | (state): ${state?.currentDayNum}")
        // log.debug("currentMonth: $currentMonth | (state): ${state?.currentMonth}")
        // log.debug("currentYear: $currentYear | (state): ${state?.currentYear}")

        def previousPower = state?.lastPower ?: currentPower
        def powerChange = (currentPower - previousPower)
        def chgStr = ""
        chgStr += powerChange > 0 ? "CurrentPower: (${currentPower}W [\u21D1${powerChange}W])" : ""
        chgStr += powerChange < 0 ? "CurrentPower: (${currentPower}W [\u21D3${powerChange.abs()}W])" : ""
        chgStr += powerChange == 0 ?"CurrentPower: (${currentPower}W [${powerChange}W])" : ""
        log.info "$chgStr || CurrentEnergy: (${currentEnergy}kWh)"
        state?.lastPower = currentPower

        def minPowerReading = state?.minPowerReading ?: null
        def maxPowerReading = state?.maxPowerReading ?: null
        if (!maxPowerReading || (maxPowerReading?.toFloat() < currentPower?.toFloat())) {
            state?.maxPowerReading = currentPower?.toInteger()
            sendEvent(name: "maxPowerReading", value: "${currentPower}", unit: "W", description: "Highest Power Reading is ${currentPower}W", display: false, displayed: false)
        }
        else if (!minPowerReading || !state?.minPowerFix || (minPowerReading?.toFloat() > currentPower?.toFloat())) {
            state?.minPowerReading = currentPower?.toInteger()
            state?.minPowerFix = true
            sendEvent(name: "minPowerReading", value: "${currentPower}", unit: "W", description: "Lowest Power Reading is ${currentPower}W", display: false, displayed: false)
        }

        def minEnergyReading = state?.minEnergyReading ?: null
        def maxEnergyReading = state?.maxEnergyReading ?: null
        if (!maxEnergyReading || (maxEnergyReading?.toFloat() < currentEnergy?.toFloat())) {
            state.maxEnergyReading = currentEnergy?.toFloat()
            sendEvent(name: "maxEnergyReading", value: "${currentEnergy}", unit: "kWh", description: "Highest Day Energy Consumption is ${currentEnergy} kWh", display: false, displayed: false)
        }
        else if (!minEnergyReading || (minEnergyReading.toFloat() > currentEnergy?.toFloat())) {
            state.minEnergyReading = currentEnergy?.toFloat()
            sendEvent(name: "minEnergyReading", value: "${currentEnergy}", unit: "kWh", description: "Lowest Day Energy Consumption is ${currentEnergy} kWh", display: false, displayed: false)
        }

        if(!state?.powerTable) { state?.powerTable = [] }
        if(!state?.energyTable) { state?.energyTable = [] }
        if(!state?.powerTableYesterday) { state.powerTableYesterday = [] }
        if(!state?.energyTableYesterday) { state.energyTableYesterday = [] }
    	if(!state?.historyStoreMap) { initHistoryStore() }

        def powerTable = state?.powerTable
        def energyTable = state?.energyTable
        if(!state?.currentDay || state?.currentDay != currentDay) {
            log.debug "currentDay ($currentDay) is != to State (${state?.currentDay})"
            state.powerTableYesterday = powerTable
            state.energyTableYesterday = energyTable
            updateHistoryData(today)
            state?.minPowerReading = curPow
            state?.maxPowerReading = curPow
            state?.minEnergyReading = curEner
            state?.maxEnergyReading = curEner

            powerTable = []
            energyTable = []
            state?.powerTable = powerTable
            state?.energyTable = energyTable
            state.currentDay = currentDay
            state.currentDayNum = currentDayNum
            state.lastPower = 0
        }

        if (currentPower.toInteger() > 0 || powerTable?.size() != 0) {
            def newDate = new Date()
            if(getLastRecUpdSec() >= 117 || state?.lastRecordDt == null ) {

                powerTable.add([newDate.format("H", location.timeZone), newDate.format("m", location.timeZone), currentPower])
                energyTable.add([newDate.format("H", location.timeZone), newDate.format("m", location.timeZone), currentEnergy])
                //log.debug "powerTable: ${powerTable}"
                state.powerTable = powerTable
            	state.energyTable = energyTable
                state.lastRecordDt = getDtNow()
                //log.debug "powerTable: $powerTable"
                //log.debug "energyTable: $energyTable"
                def dPwrAvg = getDayPowerAvg()
                if(dPwrAvg && isStateChange(device, "dayPowerAvg", dPwrAvg?.toString())) {
                    sendEvent(name: "dayPowerAvg", value: "${dPwrAvg}", unit: "W", description: "Average Power Reading today was ${dPwrAvg}W", display: false, displayed: false)
                }
            }
        }
    } catch (ex) {
        log.error("handleData Exception:", ex)
    }
}

def getObjType(obj, retType=false) {
	if(obj instanceof String) {return "String"}
	else if(obj instanceof GString) {return "GString"}
	else if(obj instanceof Map) {return "Map"}
	else if(obj instanceof List) {return "List"}
	else if(obj instanceof ArrayList) {return "ArrayList"}
	else if(obj instanceof Integer) {return "Integer"}
	else if(obj instanceof BigInteger) {return "BigInteger"}
	else if(obj instanceof Long) {return "Long"}
	else if(obj instanceof Boolean) {return "Boolean"}
	else if(obj instanceof BigDecimal) {return "BigDecimal"}
	else if(obj instanceof Float) {return "Float"}
	else if(obj instanceof Byte) {return "Byte"}
	else { return "unknown"}
}

def getLastRecUpdSec() { return state?.lastRecordDt == null ? 100000 : GetTimeDiffSeconds(state?.lastRecordDt, "getLastRecUpdSec")?.toInteger() }

def initHistoryStore() {
	Logger("initHistoryStore()...", "trace")

	def historyStoreMap = [:]
	def today = new Date()
	def dayNum = today.format("u", location.timeZone) as Integer // 1 = Monday,... 7 = Sunday
	def monthNum = today.format("MM", location.timeZone) as Integer
	def yearNum = today.format("YYYY", location.timeZone) as Integer

	//dayNum = 6   // TODO DEBUGGING

	historyStoreMap = [
		currentDay: dayNum,
		currentMonth: monthNum,
		currentYear: yearNum,
		Power_DayWeekago_usage: 0L,
        Power_DayWeekago_avgusage: 0L,
		Power_MonthYearago_usage: 0L,
        Power_MonthYearago_avgusage: 0L,
		Power_thisYear_usage: 0L,
        Power_thisYear_avgusage: 0L,
		Power_lastYear_usage: 0L,
        Power_lastYear_avgusage: 0L,
		Energy_DayWeekago_usage: 0L,
		Energy_MonthYearago_usage: 0L,
		Energy_thisYear_usage: 0L,
		Energy_lastYear_usage: 0L
	]

	for(int i = 1; i <= 7; i++) {
		historyStoreMap << ["Power_Day${i}_usage": 0L, "Power_Day${i}_avgusage": 0L]
		historyStoreMap << ["Energy_Day${i}_usage": 0L]
	}

	for(int i = 1; i <= 12; i++) {
		historyStoreMap << ["Power_Month${i}_usage": 0L]
		historyStoreMap << ["Energy_Month${i}_usage": 0L]
	}
	state.historyStoreMap = historyStoreMap
}

def updateHistoryData(today) {
    Logger("updateOperatingHistory(${today})...", "trace")

	def dayChange = false
	def monthChange = false
	def yearChange = false

	def hm = state?.historyStoreMap
	if(hm == null) {
		log.error "hm is null"
		return
	}
	def dayNum = today.format("u", location.timeZone).toInteger() // 1 = Monday,... 7 = Sunday
	def monthNum = today.format("MM", location.timeZone).toInteger()
	def yearNum = today.format("YYYY", location.timeZone).toInteger()

	if(hm?.currentDay == null || hm?.currentDay < 1 || hm?.currentDay > 7) {
		Logger("hm.currentDay is invalid (${hm?.currentDay})", "error")
		return
	}

	if(dayNum == null || dayNum < 1 || dayNum > 7) {
		Logger("dayNum is invalid (${dayNum})", "error")
		return
	}

	if(monthNum == null || monthNum < 1 || monthNum > 12) {
		Logger("monthNum is invalid (${monthNum})", "error")
		return
	}

	Logger("dayNum: ${dayNum} currentDay ${hm.currentDay} | monthNum: ${monthNum} currentMonth ${hm.currentMonth} | yearNum: ${yearNum} currentYear: ${hm.currentYear}")

	if(dayNum != hm.currentDay) {
		dayChange = true
	}
	if(monthNum != hm.currentMonth) {
		monthChange = true
	}
	if(yearNum != hm.currentYear) {
		yearChange = true
	}

	if(dayChange) {
		def power_usage = getSumUsage(state?.powerTableYesterday)
        def power_avgusage = getDayPowerAvg()
        def energy_usage = getSumUsage(state?.energyTableYesterday)

		log.info "power_usage: ${power_usage} | energy_usage: ${energy_usage}"

		hm."Power_Day${hm.currentDay}_usage" = power_usage
        hm."Power_Day${hm.currentDay}_avgusage" = power_avgusage
		hm."Energy_Day${hm.currentDay}_usage" = energy_usage

		hm.currentDay = dayNum
		hm.Power_DayWeekago_usage = hm?."Power_Day${hm.currentDay}_usage"
        hm.Power_DayWeekago_avgusage = hm?."Power_Day${hm.currentDay}_avgusage"
		hm.Energy_DayWeekago_usage = hm?."Energy_Day${hm.currentDay}_usage"
		hm."Power_Day${hm.currentDay}_usage" = 0L
        hm."Power_Day${hm.currentDay}_avgusage" = 0L
		hm."Energy_Day${hm.currentDay}_usage" = 0L

		def t1 = hm["Power_Month${hm.currentMonth}_usage"]?.toInteger() ?: 0L
        hm."Power_Month${hm.currentMonth}_usage" = t1 + power_usage
        t1 = hm["Power_Month${hm.currentMonth}_avgusage"]?.toInteger() ?: 0L
        hm."Power_Month${hm.currentMonth}_avgusage" = t1 + power_avgusage
		t1 = hm["Energy_Month${hm.currentMonth}_usage"]?.toInteger() ?: 0L
		hm."Energy_Month${hm.currentMonth}_usage" = t1 + energy_usage

		if(monthChange) {
			hm.currentMonth = monthNum
			hm.Power_MonthYearago_usage = hm?."Power_Month${hm.currentMonth}_usage"
            hm.Power_MonthYearago_avgusage = hm?."Power_Month${hm.currentMonth}_avgusage"
			hm.Energy_MonthYearago_usage = hm?."Energy_Month${hm.currentMonth}_usage"
			hm."Power_Month${hm.currentMonth}_usage" = 0L
            hm."Power_Month${hm.currentMonth}_avgusage" = 0L
			hm."Energy_Month${hm.currentMonth}_usage" = 0L
		}

		t1 = hm[Power_thisYear_usage]?.toInteger() ?: 0L
		hm.Power_thisYear_usage = t1 + power_usage
        t1 = hm[Power_thisYear_avgusage]?.toInteger() ?: 0L
		hm.Power_thisYear_avgusage = t1 + power_avgusage
		t1 = hm[Energy_thisYear_usage]?.toInteger() ?: 0L
		hm.Energy_thisYear_usage = t1 + energy_usage

		if(yearChange) {
			hm.currentYear = yearNum
			hm.Power_lastYear_usage = hm?.Power_thisYear_usage
            hm.Power_lastYear_avgusage = hm?.Power_thisYear_avgusage
			hm.Energy_lastYear_usage = hm?.Energy_thisYear_usage

			hm.Power_thisYear_usage = 0L
            hm.Power_thisYear_avgusage = 0L
			hm.Energy_thisYear_usage = 0L
		}
		state?.historyStoreMap = hm
	}
}

def getSumUsage(table) {
	log.trace "getSumUsage...Table size: ${table?.size()}"
	def total = 0L

	//log.trace "$table"
	table?.each() {
        if(it[2] != null && it[2]?.isNumber() && it[2].toDouble() < 0) {
		    total = total + it[2].toDouble()
        }
	}
    log.debug "total: $total"
	return total.toDouble()
}

def getDayElapSec() {
	Calendar c = Calendar.getInstance();
	long now = c?.getTimeInMillis();
	c.set(Calendar?.HOUR_OF_DAY, 0);
	c.set(Calendar?.MINUTE, 0);
	c.set(Calendar?.SECOND, 0);
	c.set(Calendar?.MILLISECOND, 0);
	long passed = now - c?.getTimeInMillis();
	return (long) passed / 1000;
}

def getDayPowerAvg() {
    try {
        def result = null
        if(state?.powerTable?.size() >= 2) {
            def avgTmp = []
            state?.powerTable?.each() {
                if(it[2] != null) {
                    avgTmp?.push(it[2])
                }
            }
            if(avgTmp?.size() >= 2) {
                result = getListAvg(avgTmp).toInteger()
            }
        }
        return result
    } catch (ex) {
        log.error "getDayPowerAvg Exception:", ex
    }
}

def getAverage(items) {
    def tmpAvg = []
    def tmpVal = 0
	if(!items) { return tmpVal }
	else if(items?.size() > 1) {
		tmpAvg = items
		if(tmpAvg && tmpAvg?.size() > 1) { tmpVal = (tmpAvg?.sum() / tmpAvg?.size()) }
	}
	return tmpVal
}

def getListAvg(itemList, rnd=0) {
	def avgRes = 0.0
	def iCnt = itemList?.size()
	if(iCnt >= 1) {
		if(iCnt > 1) {
			avgRes = (itemList?.sum()?.toDouble() / iCnt?.toDouble())?.toDouble()
		} else { itemList?.each { avgRes = avgRes + it?.toDouble() } }
	}
	// log.debug "getListAvg | avgRes: $avgRes"
    return avgRes.round(rnd)
}

def updateAttributes(rData, uData, tData, hData) {
    //log.trace "updateAttributes( $rData, $uData, $tData, $hData )"
    def readDate = Date.parse("MMM d,yyyy - h:mm:ss a", rData?.readingUpdated).format("MMM d,yyyy")
    def readTime = Date.parse("MMM d,yyyy - h:mm:ss a", rData?.readingUpdated).format("h:mm:ss a")
    def curDolSym = state?.currency?.dollar.toString()
    def curCentSym = state?.currency?.cent.toString()
    def currentEnergy = uData?.todayUsage
    def tariffRate = tData.tariffRate.isNumber() ? (tData.tariffRate.toDouble()/100) : 0.0
    logWriter("--------------UPDATE READING DATA-------------")
    logWriter("energy: ${currentEnergy} kWh")
    logWriter("power: ${rData?.powerReading}W")
    logWriter("readingUpdated: ${rData?.readingUpdated}")
    logWriter("")
    //Updates Device Readings to tiles
    if(isStateChange(device, "energy", currentEnergy.toString())) {
        sendEvent(name: "energy", unit: "kWh", value: currentEnergy, description: "Energy Value is ${currentEnergy} kWh", display: false, displayed: false)
    }
    if(isStateChange(device, "power", rData.powerReading.toString())) {
        sendEvent(name: "power", unit: "W", value: rData?.powerReading, description: "Power Value is ${rData?.powerReading} W", display: false, displayed: false)
    }
    if(isStateChange(device, "readingUpdated", rData?.readingUpdated.toString())) {
        sendEvent(name: "readingUpdated", value: rData?.readingUpdated, description: "Reading Updated at ${rData?.readingUpdated}", display: false, displayed: false)
    }
    if(isStateChange(device, "readingUpdated_str", "Last Updated:\n${readDate}\n${readTime}")) {
        sendEvent(name: "readingUpdated_str", value: "Last Updated:\n${readDate}\n${readTime}", display: false, displayed: false)
    }

    //UPDATES USAGE INFOR
    def budgPercent
    logWriter("--------------UPDATE USAGE DATA-------------")
    logWriter("todayUsage: ${uData?.todayUsage} kWh")
    logWriter("todayCost: ${curDolSym}${uData?.todayCost}")
    logWriter("monthUsage: ${uData?.monthUsage} kWh")
    logWriter("monthCost: ${curDolSym}${uData?.monthCost}")
    logWriter("monthEst: ${curDolSym}${uData?.monthEst}")
    logWriter("monthBudget: ${curDolSym}${uData?.monthBudget}")

    sendEvent(name: "todayUsage_str", value: "${curDolSym}${uData?.todayCost} (${uData?.todayUsage} kWH)", display: false, displayed: false)
    sendEvent(name: "monthUsage_str", value: "${state?.monthName}\'s Usage:\n${curDolSym}${uData?.monthCost} (${uData?.monthUsage} kWh)", display: false, displayed: false)
    sendEvent(name: "monthEst_str",   value: "${state?.monthName}\'s Bill (Est.):\n${curDolSym}${uData?.monthEst}", display: false, displayed: false)
    sendEvent(name: "todayUsage", value: uData?.todayUsage.toString(), unit: "kWh", display: false, displayed: false)
    sendEvent(name: "todayCost",   value: uData?.todayCost.toString(), unit: curDolSym, display: false, displayed: false)
    sendEvent(name: "weekUsage", value: uData?.weekUsage.toString(), unit: "kWh", display: false, displayed: false)
    sendEvent(name: "weekCost",   value: uData?.weekCost.toString(), unit: curDolSym, display: false, displayed: false)
    sendEvent(name: "monthUsage", value: uData?.monthUsage.toString(), unit: "kWh", display: false, displayed: false)
    sendEvent(name: "monthCost",   value: uData?.monthCost.toString(), unit: curDolSym, display: false, displayed: false)
    sendEvent(name: "monthEst",   value: uData?.monthEst.toString(), unit: curDolSym, display: false, displayed: false)
    sendEvent(name: "yearUsage", value: uData?.yearUsage.toString(), unit: "kWh", display: false, displayed: false)
    sendEvent(name: "yearCost",   value: uData?.yearCost.toString(), unit: curDolSym, display: false, displayed: false)


    if (uData?.monthBudget > 0) {
        budgPercent = Math.round(Math.round(uData?.monthCost?.toFloat()) / Math.round(uData?.monthBudget?.toFloat()) * 100)
        sendEvent(name: "budgetPercentage_str", value: "Monthly Budget:\nUsed ${budgPercent}% (${curDolSym}${uData?.monthCost}) of ${curDolSym}${uData?.monthBudget} ", display: false, displayed: false)
        sendEvent(name: "budgetPercentage", value: budgPercent.toString(), unit: "%", description: "Percentage of Budget User is (${budgPercent}%)", display: false, displayed: false)
    } else {
        budgPercent = 0
        sendEvent(name: "budgetPercentage_str", value: "Monthly Budget:\nBudget Not Set...", display: false, displayed: false)
    }
    logWriter("Budget Percentage: ${budgPercent}%")
    logWriter("")

    //Tariff Info
    logWriter("--------------UPDATE TARIFF DATA-------------")
    logWriter("tariff rate: ${tariffRate}${curCentSym}")
    logWriter("")
    sendEvent(name: "tariffRate", value: tariffRate, unit: curCentSym, description: "Tariff Rate is ${tariffRate}${curCentSym}/kWh", display: false, displayed: false)
    sendEvent(name: "tariffRate_str", value: "Tariff Rate:\n${tariffRate}${curCentSym}/kWh", description: "Tariff Rate is ${tariffRate}${curCentSym}/kWh", display: false, displayed: false)

    //Updates Hub INFO Tiles
    logWriter("--------------UPDATE HUB DATA-------------")
    logWriter("hubVersion: " + hData?.hubVersion)
    logWriter("hubStatus: " + hData?.hubStatus)
    logWriter("hubName: " + hData?.hubName)
    logWriter("")
    state.hubStatus = (hData?.hubStatus == "on") ? "Active" : "InActive"
    state.hubVersion = hData?.hubVersion
    state.hubName = hData?.hubName
    sendEvent(name: "hubVersion", value: hData?.hubVersion, display: false, displayed: false)
    sendEvent(name: "hubStatus", value: hData?.hubStatus, display: false, displayed: false)
    sendEvent(name: "hubName", value: hData?.hubName, display: false, displayed: false)
}

def lastCheckinEvent(checkin) {
    //log.trace "lastCheckinEvent($checkin)..."
    def formatVal = "MMM d, yyyy - h:mm:ss a"
    def tf = new SimpleDateFormat(formatVal)
        tf.setTimeZone(location.timeZone)
    def lastConn = checkin ? "${tf?.format(Date.parse("E MMM dd HH:mm:ss z yyyy", checkin))}" : "Not Available"
    def lastChk = device.currentState("lastConnection")?.value
    state?.lastConnection = lastConn?.toString()
    if(isStateChange(device, "lastConnection", lastConn?.toString())) {
        logWriter("UPDATED | Last Hub Check-in was: (${lastConn}) | Original State: (${lastChk})")
        sendEvent(name: 'lastConnection', value: lastConn?.toString(), displayed: false, isStateChange: true)
    } else { logWriter("Last Hub Check-in was: (${lastConn}) | Original State: (${lastChk})") }
}

def lastUpdatedEvent() {
    def now = new Date()
    def formatVal = "MMM d, yyyy - h:mm:ss a"
    def tf = new SimpleDateFormat(formatVal)
    tf.setTimeZone(location.timeZone)
    def lastDt = "${tf?.format(now)}"
    def lastUpd = device.currentState("lastUpdatedDt")?.value
    state?.lastUpdatedDt = lastDt?.toString()
    if(isStateChange(device, "lastUpdatedDt", lastDt?.toString())) {
        logWriter("Last Parent Refresh time: (${lastDt}) | Previous Time: (${lastUpd})")
        sendEvent(name: 'lastUpdatedDt', value: lastDt?.toString(), displayed: false, isStateChange: true)
    }
}

def debugOnEvent(debug) {
    def val = device.currentState("debugOn")?.value
    def dVal = debug ? "On" : "Off"
    state?.debugStatus = dVal
    //log.debug "debugStatus: ${state?.debugStatus}"
    state?.debug = debug.toBoolean() ? true : false
    if(isStateChange(device, "debugOn", dVal)) {
        log.debug("UPDATED | debugOn: (${dVal}) | Original State: (${val.toString().capitalize()})")
        sendEvent(name: 'debugOn', value: dVal, displayed: false)
    } else { logWriter("debugOn: (${dVal}) | Original State: (${val})") }
}

def deviceVerEvent(ver) {
    def curData = device.currentState("devTypeVer")?.value.toString()
    def pubVer = ver ?: null
    def dVer = devTypeVer() ?: null
    def newData = isCodeUpdateAvailable(pubVer, dVer) ? "${dVer}(New: v${pubVer})" : "${dVer}"
    state?.devTypeVer = newData
    //log.debug "devTypeVer: ${state?.devTypeVer}"
    state?.updateAvailable = isCodeUpdateAvailable(pubVer, dVer)
    if(isStateChange(device, "devTypeVer", newData)) {
        logWriter("UPDATED | Device Type Version is: (${newData}) | Original State: (${curData})")
        sendEvent(name: 'devTypeVer', value: newData, displayed: false)
    } else { logWriter("Device Type Version is: (${newData}) | Original State: (${curData})") }
}

def apiStatusEvent(issue) {
    def curStat = device.currentState("apiStatus")?.value
    def newStat = issue ? "Problems" : "Good"
    state?.apiStatus = newStat
    //log.debug "apiStatus: ${state?.apiStatus}"
    if(isStateChange(device, "apiStatus", newStat.toString())) {
        log.debug("UPDATED | API Status is: (${newStat.toString().capitalize()}) | Original State: (${curStat.toString().capitalize()})")
        sendEvent(name: "apiStatus", value: newStat, descriptionText: "API Status is: ${newStat}", displayed: true, isStateChange: true, state: newStat)
    } else { logWriter("API Status is: (${newStat}) | Original State: (${curStat})") }
}

def getEnergy() { return !device.currentValue("energy") ? 0 : device.currentValue("energy") }
def getPower() { return !device.currentValue("power") ? 0 : device.currentValue("power") }
def getStateSize() { return state?.toString().length() }
def getStateSizePerc() { return (int) ((stateSize/100000)*100).toDouble().round(0) }
def getDataByName(String name) { state[name] ?: device.getDataValue(name) }
def getDeviceStateData() { return getState() }

def isCodeUpdateAvailable(newVer, curVer) {
    def result = false
    def latestVer
    def versions = [newVer, curVer]
    if(newVer != curVer) {
        latestVer = versions?.max { a, b ->
            def verA = a?.tokenize('.')
            def verB = b?.tokenize('.')
            def commonIndices = Math.min(verA?.size(), verB?.size())
            for (int i = 0; i < commonIndices; ++i) {
                if (verA[i]?.toInteger() != verB[i]?.toInteger()) {
                    return verA[i]?.toInteger() <=> verB[i]?.toInteger()
                }
            }
            verA?.size() <=> verB?.size()
        }
        result = (latestVer == newVer) ? true : false
    }
    //log.debug "type: $type | newVer: $newVer | curVer: $curVer | newestVersion: ${latestVer} | result: $result"
    return result
}

def getTimeZone() {
	if (location.timeZone != null) {
		return location.timeZone
	} else { log.warn("getTimeZone: SmartThings TimeZone is not found on your account...") }
	return null
}

def formatDt(dt) {
    def tf = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
    if(location.timeZone) { tf.setTimeZone(location.timeZone) }
    else { log.warn "SmartThings TimeZone is not found or is not set... Please Try to open your ST location and Press Save..." }
    return tf?.format(dt)
}

//Returns time differences is seconds
def GetTimeDiffSeconds(lastDate, sender=null) {
    //log.trace "GetTimeDiffSeconds($lastDate, $sendera)"
    try {
        if(lastDate?.contains("dtNow")) { return 10000 }
        def now = new Date()
        def lastDt = Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate)
        def start = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(lastDt)).getTime()
        def stop = Date.parse("E MMM dd HH:mm:ss z yyyy", formatDt(now)).getTime()
        def diff = (int) (long) (stop - start) / 1000
        return diff
    }
    catch (ex) {
        log.error "GetTimeDiffSeconds Exception: (${sender ? "$sender | " : ""}lastDate: $lastDate):", ex
        return 10000
    }
}

def getDtNow() {
	def now = new Date()
	return formatDt(now)
}

def getTzOffSet() {
	def val = location?.timeZone?.getRawOffset()
	val = val/1000/60
	state?.tzOffsetVal = val ?: 0
}

//Log Writer that all logs are channel through *It will only output these if Debug Logging is enabled under preferences
private def logWriter(value) {
    if (state.debug) {
        log.debug "${value}"
    }
}

def Logger(msg, type="debug") {
    if(msg && type) {
        switch(type) {
            case "debug":
                log.debug "${msg}"
                break
            case "info":
                log.info "${msg}"
                break
            case "trace":
                   log.trace "${msg}"
                break
            case "error":
                log.error "${msg}"
                break
            case "warn":
                log.warn "${msg}"
                break
            default:
                log.debug "${msg}"
                break
        }
    }
}

/*************************************************************
|                  HTML TILE RENDER FUNCTIONS                |
**************************************************************/
String getDataString(Integer seriesIndex) {
	def dataString = ""
	def dataTable = []
	switch (seriesIndex) {
		case 1:
			dataTable = state.energyTableYesterday
			break
		case 2:
            dataTable = state.powerTableYesterday
			break
        case 3:
			dataTable = state.energyTable
			break
        case 4:
			dataTable = state.powerTable
			break
	}
	dataTable.each() {
		def dataArray = [[it[0],it[1],0],null,null,null,null]
		dataArray[seriesIndex] = it[2]
		dataString += dataArray.toString() + ","
	}
	return dataString
}

def getImg(imgName) { return imgName ? "https://raw.githubusercontent.com/tonesto7/efergy-manager/master/Images/Devices/$imgName" : "" }

def getStartTime() {
	def startTime = 24
	if (state?.powerTable.size()) { startTime = state?.powerTable?.min{it[0].toInteger()}[0].toInteger() }
	if (state?.powerTableYesterday.size()) { startTime = Math.min(startTime, state?.powerTableYesterday?.min{it[0].toInteger()}[0].toInteger())	}
	return startTime
    LogAction("startTime ${startTime}", "trace")
}

def getMinVal(Integer item) {
    def list = []
    if (state?.usageTableYesterday?.size() > 0) { list.add(state?.usageTableYesterday?.min { it[item] }[item].toInteger()) }
    if (state?.usageTable?.size() > 0) { list.add(state?.usageTable.min { it[item] }[item].toInteger()) }
    //log.trace "getMinVal: ${list.min()} result: ${list}"
    return list?.min()
}

def getMaxVal(Integer item) {
    def list = []
    if (state?.usageTableYesterday && state?.usageTableYesterday?.size() > 0) { list.add(state?.usageTableYesterday.max { it[item] }[item].toInteger()) }
    if (state?.usageTable && state?.usageTable?.size() > 0) { list.add(state?.usageTable.max { it[item] }[item].toInteger()) }
    //log.trace "getMaxVal: ${list.max()} result: ${list}"
    return list?.max()
}

def getGraphHTML() {
    try {
        def updateAvail = !state?.updateAvailable ? "" : """<h3 style="background: #ffa500;">Device Update Available!</h3>"""
        def chartHtml = ((state?.powerTable && state?.powerTable.size() > 0) || (state?.energyTable && state?.energyTable?.size() > 0)) ? showChartHtml() : hideChartHtml()
        def refreshBtnHtml = """<div class="pageFooterBtn"><button type="button" class="btn btn-info pageFooterBtn" onclick="reloadPage()"><span>&#10227</span> Refresh</button></div>"""
        def html = """
        <!DOCTYPE html>
        <html>
            <head>
                <meta http-equiv="cache-control" content="max-age=0"/>
                <meta http-equiv="cache-control" content="no-cache"/>
                <meta http-equiv="expires" content="0"/>
                <meta http-equiv="expires" content="Tue, 01 Jan 1980 1:00:00 GMT"/>
                <meta http-equiv="pragma" content="no-cache"/>
                <meta name="viewport" content="width = device-width, user-scalable=no, initial-scale=1.0">
                <link rel="stylesheet" href="https://cdn.rawgit.com/tonesto7/efergy-manager/master/resources/style.css"/>
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <style>
                    .pageFooterBtn {
                        padding: 10px;
                        horizontal-align: center;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                ${updateAvail}

                ${chartHtml}

                <br></br>
                <table>
                <col width="49%">
                <col width="49%">
                <thead>
                  <th>Hub Status</th>
                  <th>API Status</th>
                </thead>
                <tbody>
                  <tr>
                    <td>${state?.hubStatus}</td>
                    <td>${state?.apiStatus}</td>
                  </tr>
                </tbody>
              </table>
              <table>
                <tr>
                  <th>Hub Name</th>
                </tr>
                <td>${state?.hubName}</td>
                </tbody>
              </table>
              <table>
                <tr>
                  <th>Hub Version</th>
                  <th>Debug</th>
                  <th>Device Type</th>
                </tr>
                <td>${state?.hubVersion.toString()}</td>
                <td>${state?.debugStatus}</td>
                <td>${state?.devTypeVer.toString()}</td>
                </tbody>
              </table>
              <table>
                <thead>
                  <th>Hub Checked-In</th>
                  <th>Data Last Received</th>
                </thead>
                <tbody>
                  <tr>
                    <td class="dateTimeText">${state?.lastConnection.toString()}</td>
                    <td class="dateTimeText">${state?.lastUpdatedDt.toString()}</td>
                  </tr>
              </table>
              <script>
                function reloadPage() {
                  var url = "https://" + window.location.host + "/api/devices/${device?.getId()}/graphHTML"
                  window.location = url;
                }
              </script>
              ${refreshBtnHtml}
            </body>
        </html>
        """
        render contentType: "text/html", data: html, status: 200
    } catch (ex) {
        log.error "graphHTML Exception:", ex
    }
}

def showChartHtml() {
    try {
        def data = """
        <script type="text/javascript">
          google.charts.load('current', {packages: ['corechart']});
          google.charts.setOnLoadCallback(drawGraph);
          function drawGraph() {
          var data = new google.visualization.DataTable();
          data.addColumn('timeofday', 'time');
          data.addColumn('number', 'Energy (Yesterday)');
          data.addColumn('number', 'Power (Yesterday)');
          data.addColumn('number', 'Energy (Today)');
          data.addColumn('number', 'Power (Today)');
          data.addRows([
            ${getDataString(1)}
            ${getDataString(2)}
            ${getDataString(3)}
            ${getDataString(4)}
          ]);
          var options = {
            fontName: 'San Francisco, Roboto, Arial',
            width: '100%',
            height: '100%',
            isStacked: 'absolute',
            animation: {
              duration: 2500,
              startup: true,
              easing: 'inAndOut'
            },
            hAxis: {
              format: 'H:mm',
              minValue: [${getStartTime()},0,0],
              slantedText: true,
              slantedTextAngle: 30
            },
            series: {
                0: {targetAxisIndex: 1, color: '#cbe5a9', lineWidth: 1, visibleInLegend: false},
                1: {targetAxisIndex: 0, color: '#fcd4a2', lineWidth: 1, visibleInLegend: false},
                2: {targetAxisIndex: 1, color: '#8CC640'},
                3: {targetAxisIndex: 0, color: '#F8971D'}
            },
            vAxes: {
                0: {
                  title: 'Power Used (W)',
                  format: 'decimal',
                  textStyle: {color: '#F8971D'},
                  titleTextStyle: {color: '#F8971D'}
                },
                1: {
                  title: 'Energy Consumed (kWh)',
                  format: 'decimal',
                  textStyle: {color: '#8CC640'},
                  titleTextStyle: {color: '#8CC640'}
                }
            },
            legend: {
              position: 'none',
              maxLines: 4
            },
            chartArea: {
              left: '12%',
              right: '15%',
              top: '5%',
              bottom: '15%',
              height: '100%',
              width: '100%'
            },
            displayAnnotations: false
          };
          var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
          chart.draw(data, options);
        }
          </script>
          <h4 style="font-size: 22px; font-weight: bold; text-align: center; background: #8CC640; color: #f5f5f5;">Usage History</h4>
          <div id="chart_div" style="width: 100%; height: 225px;"></div>
        """
        return data
    } catch (ex) {
        log.error "showChartHtml Exception:", ex
    }
}

def hideChartHtml() {
    try {
        def data = """
        <h4 style="font-size: 22px; font-weight: bold; text-align: center; background: #8CC640; color: #f5f5f5;">Usage History</h4>
        <br></br>
        <div class="centerText">
          <p>Waiting for more data to be collected...</p>
          <p>This may take a little while...</p>
        </div>
        """
        return data
    } catch (ex) {
        log.error "showChartHtml Exception:", ex
    }
}
